package com.koch.ambeth.bytecode.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

import com.koch.ambeth.bytecode.IBuildVisitorDelegate;
import com.koch.ambeth.bytecode.IBytecodeClassLoader;
import com.koch.ambeth.bytecode.behavior.BytecodeBehaviorState;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorExtendable;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import com.koch.ambeth.bytecode.config.BytecodeConfigurationConstants;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.bytecode.IBytecodeEnhancer;
import com.koch.ambeth.ioc.bytecode.IEnhancementHint;
import com.koch.ambeth.ioc.bytecode.ITargetNameEnhancementHint;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.extendable.ExtendableContainer;
import com.koch.ambeth.ioc.extendable.IExtendableContainer;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.proxy.IEnhancedType;
import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IdentityLinkedSet;
import com.koch.ambeth.util.collections.SmartCopyMap;
import com.koch.ambeth.util.collections.WeakSmartCopyMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

public class BytecodeEnhancer
		implements IBytecodeEnhancer, IBytecodeBehaviorExtendable, IStartingBean {
	public static class ValueType extends SmartCopyMap<IEnhancementHint, Reference<Class<?>>> {
		private volatile int changeCount = 0;

		public void addChangeCount() {
			changeCount++;
		}

		public int getChangeCount() {
			return changeCount;
		}
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IBytecodeClassLoader bytecodeClassLoader;

	@Autowired(optional = true)
	protected IBytecodeStore bytecodeStore;

	@Property(name = BytecodeConfigurationConstants.EnhancementTraceDirectory, mandatory = false)
	protected String traceDir;

	protected final WeakSmartCopyMap<Class<?>, ValueType> typeToExtendedType =
			new WeakSmartCopyMap<Class<?>, ValueType>();

	protected final WeakSmartCopyMap<Class<?>, Reference<Class<?>>> extendedTypeToType =
			new WeakSmartCopyMap<Class<?>, Reference<Class<?>>>();

	protected final HashSet<Class<?>> supportedEnhancements = new HashSet<Class<?>>(0.5f);

	protected final Lock writeLock = new ReentrantLock();

	protected final IExtendableContainer<IBytecodeBehavior> bytecodeBehaviorExtensions =
			new ExtendableContainer<IBytecodeBehavior>(IBytecodeBehavior.class, "bytecodeBehavior");

	protected Map<BytecodeStoreKey, BytecodeStoreItem> enhancedTypes;

	public BytecodeEnhancer() {
		extendedTypeToType.setAutoCleanupNullValue(true);
	}

	@Override
	public void afterStarted() throws Throwable {
		if (bytecodeStore != null) {
			enhancedTypes =
					bytecodeStore.loadEnhancedTypes(this, bytecodeBehaviorExtensions.getExtensions());
		}
	}

	@Override
	public boolean supportsEnhancement(Class<?> enhancementType) {
		writeLock.lock();
		try {
			return supportedEnhancements.contains(enhancementType);
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public boolean isEnhancedType(Class<?> entityType) {
		return IEnhancedType.class.isAssignableFrom(entityType);
	}

	@Override
	public Class<?> getBaseType(Class<?> enhancedType) {
		if (!isEnhancedType(enhancedType)) {
			return null;
		}
		Reference<Class<?>> typeR = extendedTypeToType.get(enhancedType);
		if (typeR == null) {
			throw new IllegalStateException("Must never happen");
		}
		Class<?> type = typeR.get();
		if (type == null) {
			throw new IllegalStateException("Must never happen");
		}
		return type;
	}

	protected Class<?> getEnhancedTypeIntern(Class<?> entityType, IEnhancementHint enhancementHint) {
		Reference<Class<?>> existingBaseTypeR = extendedTypeToType.get(entityType);
		if (existingBaseTypeR != null) {
			Class<?> existingBaseType = existingBaseTypeR.get();
			if (existingBaseType != null) {
				// there is already an enhancement of the given baseType. Now we check if the existing
				// enhancement is made with the same enhancementHint
				ValueType valueType = typeToExtendedType.get(existingBaseType);
				if (valueType != null && valueType.containsKey(enhancementHint)) {
					// do nothing: the given entity is already the result of the enhancement of the
					// existingBaseType with the given enhancementHint
					// it is not possible to enhance the same two times
					return entityType;
				}
			}
		}
		ValueType valueType = typeToExtendedType.get(entityType);
		if (valueType == null) {
			return getEnhancedTypeFromStore(entityType, enhancementHint);
		}
		Reference<Class<?>> extendedTypeR = valueType.get(enhancementHint);
		if (extendedTypeR == null) {
			return getEnhancedTypeFromStore(entityType, enhancementHint);
		}
		return extendedTypeR.get();
	}

	protected Class<?> getEnhancedTypeFromStore(Class<?> entityType,
			IEnhancementHint enhancementHint) {
		BytecodeStoreItem bytecodeStoreItem = enhancedTypes != null
				? enhancedTypes.get(new BytecodeStoreKey(entityType, enhancementHint)) : null;
		if (bytecodeStoreItem == null) {
			return null;
		}
		Class<?> enhancedType = bytecodeStoreItem.readEnhancedType(bytecodeClassLoader);
		ValueType valueType = typeToExtendedType.get(entityType);
		if (valueType == null) {
			valueType = new ValueType();
			typeToExtendedType.put(entityType, valueType);
		}
		valueType.put(enhancementHint, new WeakReference<Class<?>>(enhancedType));
		return enhancedType;
	}

	@Override
	public Class<?> getEnhancedType(Class<?> typeToEnhance, IEnhancementHint hint) {
		ITargetNameEnhancementHint targetNameHint = hint.unwrap(ITargetNameEnhancementHint.class);
		if (targetNameHint == null && hint instanceof ITargetNameEnhancementHint) {
			targetNameHint = (ITargetNameEnhancementHint) hint;
		}
		String newTypeNamePrefix = typeToEnhance.getName();
		if (targetNameHint != null) {
			newTypeNamePrefix = targetNameHint.getTargetName(typeToEnhance);
		}
		return getEnhancedType(typeToEnhance, newTypeNamePrefix, hint);
	}

	protected void logBytecodeOutput(String typeName, String bytecodeOutput) {
		File outputFileDir = new File(traceDir, getClass().getName());
		outputFileDir.mkdirs();
		File outputFile = new File(outputFileDir, typeName + ".txt");
		try {
			OutputStreamWriter fw =
					new OutputStreamWriter(new FileOutputStream(outputFile), Charset.forName("UTF-8"));
			try {
				fw.write(bytecodeOutput);
			}
			finally {
				fw.close();
			}
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e,
					"Error occurred while trying to write to '" + outputFile.getAbsolutePath() + "'");
		}
	}

	@Override
	public Class<?> getEnhancedType(Class<?> typeToEnhance, String newTypeNamePrefix,
			IEnhancementHint hint) {
		Class<?> extendedType = getEnhancedTypeIntern(typeToEnhance, hint);
		if (extendedType != null) {
			return extendedType;
		}
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			// Concurrent thread may have been faster
			extendedType = getEnhancedTypeIntern(typeToEnhance, hint);
			if (extendedType != null) {
				return extendedType;
			}
			if (log.isInfoEnabled()) {
				log.info("Enhancing " + typeToEnhance + " with hint: " + hint);
			}
			ValueType valueType = typeToExtendedType.get(typeToEnhance);
			if (valueType == null) {
				valueType = new ValueType();
				typeToExtendedType.put(typeToEnhance, valueType);
			}
			else {
				valueType.addChangeCount();
				newTypeNamePrefix += "_O" + valueType.getChangeCount();
			}
			ArrayList<IBytecodeBehavior> pendingBehaviors = new ArrayList<IBytecodeBehavior>();

			IBytecodeBehavior[] allBehaviors = bytecodeBehaviorExtensions.getExtensions();
			pendingBehaviors.addAll(allBehaviors);

			ArrayList<Class<?>> enhancedTypesPipeline = new ArrayList<Class<?>>();
			Class<?> enhancedType;
			if (pendingBehaviors.size() > 0) {
				enhancedType = enhanceTypeIntern(typeToEnhance, newTypeNamePrefix, pendingBehaviors, hint,
						enhancedTypesPipeline);
			}
			else {
				enhancedType = typeToEnhance;
			}
			Reference<Class<?>> entityTypeR = typeToExtendedType.getWeakReferenceEntry(typeToEnhance);
			if (entityTypeR == null) {
				throw new IllegalStateException("Must never happen");
			}
			if (traceDir != null) {
				logBytecodeOutput(enhancedType.getName(),
						bytecodeClassLoader.toPrintableBytecode(enhancedType));
			}
			else if (log.isDebugEnabled()) {
				// note that this intentionally will only be logged to the console if the traceDir is NOT
				// specified already
				log.debug(bytecodeClassLoader.toPrintableBytecode(enhancedType));
			}
			try {
				checkEnhancedTypeConsistency(enhancedType);
			}
			catch (Throwable e) {
				if (log.isErrorEnabled()) {
					log.error(bytecodeClassLoader.toPrintableBytecode(enhancedType), e);
				}
				throw RuntimeExceptionUtil.mask(e);
			}

			WeakReference<Class<?>> enhancedTypeR = new WeakReference<Class<?>>(enhancedType);
			valueType.put(hint, enhancedTypeR);
			extendedTypeToType.put(enhancedType, entityTypeR);

			if (bytecodeStore != null) {
				bytecodeStore.storeEnhancedType(this, allBehaviors, typeToEnhance, hint, enhancedType,
						enhancedTypesPipeline);
			}
			if (log.isInfoEnabled()) {
				log.info("Enhancement finished successfully with type: " + enhancedType);
			}
			return enhancedType;
		}
		finally {
			writeLock.unlock();
		}
	}

	protected void checkEnhancedTypeConsistency(Class<?> type) {
		IdentityLinkedSet<Method> allMethods = new IdentityLinkedSet<Method>();
		for (Class<?> interf : type.getInterfaces()) {
			allMethods.addAll(interf.getMethods());
		}
		Class<?> currType = type;
		while (currType != Object.class && currType != null) {
			allMethods.addAll(currType.getDeclaredMethods());
			currType = currType.getSuperclass();
		}
		if (allMethods.size() == 0) {
			throw new IllegalStateException("Type invalid (not a single method): " + type);
		}
		if (type.getDeclaredConstructors().length == 0) {
			throw new IllegalStateException("Type invalid (not a single constructor): " + type);
		}
		if (!Modifier.isAbstract(type.getModifiers())) {
			for (Method method : allMethods) {
				Method method2 = ReflectUtil.getDeclaredMethod(true, type, method.getReturnType(),
						method.getName(), method.getParameterTypes());
				if (method2 == null || Modifier.isAbstract(method2.getModifiers())) {
					// FIXME: if method2 is null, the following exception text is misleading
					throw new IllegalStateException(
							"Type is not abstract but has at least one abstract method: " + method);
				}
			}
		}
		Class<?>[] interfaces = type.getInterfaces();
		for (Class<?> interf : interfaces) {
			Method[] interfaceMethods = ReflectUtil.getDeclaredMethods(interf);
			for (Method interfaceMethod : interfaceMethods) {
				try {
					type.getMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes());
				}
				catch (NoSuchMethodException e) {
					throw new IllegalStateException(
							"Type is not abstract but has at least one abstract method: " + interfaceMethod);
				}
			}
		}
	}

	@SuppressWarnings("resource")
	protected Class<?> enhanceTypeIntern(Class<?> originalType, String newTypeNamePrefix,
			final IList<IBytecodeBehavior> pendingBehaviors, final IEnhancementHint hint,
			List<Class<?>> enhancedTypesPipeline) {
		if (pendingBehaviors.size() == 0) {
			return originalType;
		}
		Reference<Class<?>> entityTypeR = typeToExtendedType.getWeakReferenceEntry(originalType);
		if (entityTypeR == null) {
			throw new IllegalStateException("Must never happen");
		}
		String lastTypeHandleName = newTypeNamePrefix;
		newTypeNamePrefix = newTypeNamePrefix.replaceAll(Pattern.quote("."), "/");
		final StringWriter sw = new StringWriter();
		try {
			Class<?> currentType = originalType;
			if (currentType.isInterface()) {
				currentType = Object.class;
			}
			for (int a = 0, size = pendingBehaviors.size(); a < size; a++) {
				Class<?> newCurrentType =
						pendingBehaviors.get(a).getTypeToExtendFrom(originalType, currentType, hint);
				if (newCurrentType != null) {
					currentType = newCurrentType;
				}
			}
			int iterationCount = 0;

			ArrayList<BytecodeBehaviorState> pendingStatesToPostProcess =
					new ArrayList<BytecodeBehaviorState>();
			byte[] currentContent = bytecodeClassLoader.readTypeAsBinary(currentType);
			while (pendingBehaviors.size() > 0) {
				iterationCount++;

				Type newTypeHandle = Type.getObjectType(newTypeNamePrefix + "$A" + iterationCount);
				lastTypeHandleName = newTypeHandle.getClassName();

				final IBytecodeBehavior[] currentPendingBehaviors =
						pendingBehaviors.toArray(IBytecodeBehavior.class);
				pendingBehaviors.clear();

				if (currentPendingBehaviors.length > 0 && log.isDebugEnabled()) {
					log.debug("Applying behaviors on " + newTypeHandle.getClassName() + ": "
							+ Arrays.toString(currentPendingBehaviors));
				}
				final BytecodeEnhancer This = this;
				final byte[] fCurrentContent = currentContent;

				final ParamHolder<BytecodeBehaviorState> acquiredState =
						new ParamHolder<BytecodeBehaviorState>();
				byte[] newContent = BytecodeBehaviorState.setState(originalType, currentType, newTypeHandle,
						beanContext, hint, new IResultingBackgroundWorkerDelegate<byte[]>() {
							@Override
							public byte[] invoke() throws Throwable {
								acquiredState.setValue((BytecodeBehaviorState) BytecodeBehaviorState.getState());
								return This.executePendingBehaviors(fCurrentContent, sw, currentPendingBehaviors,
										pendingBehaviors);
							}
						});
				if (newContent == null) {
					if (pendingBehaviors.size() > 0) {
						// "fix" the iterationCount to have a consistent class name hierarchy
						iterationCount--;
						continue;
					}
					return currentType;
				}
				Class<?> newType =
						bytecodeClassLoader.loadClass(newTypeHandle.getInternalName(), newContent);
				extendedTypeToType.put(newType, entityTypeR);
				pendingStatesToPostProcess.add(acquiredState.getValue());
				currentContent = newContent;
				currentType = newType;
				enhancedTypesPipeline.add(currentType);
			}
			for (int a = 0, size = pendingStatesToPostProcess.size(); a < size; a++) {
				pendingStatesToPostProcess.get(a).postProcessCreatedType(currentType);
			}
			return currentType;
		}
		catch (Throwable e) {
			String classByteCode = sw.toString();
			if (classByteCode.length() > 0) {
				if (traceDir != null) {
					logBytecodeOutput(lastTypeHandleName, classByteCode);
				}
				else {
					throw RuntimeExceptionUtil.mask(e, "Bytecode:\n" + classByteCode);
				}
			}
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	public byte[] executePendingBehaviors(byte[] currentContent, Writer sw,
			final IBytecodeBehavior[] pendingBehaviors,
			final List<IBytecodeBehavior> cascadePendingBehaviors) throws Throwable {
		final IBytecodeBehaviorState state = BytecodeBehaviorState.getState();
		byte[] content = bytecodeClassLoader.buildTypeFromParent(state.getNewType().getInternalName(),
				currentContent, sw, new IBuildVisitorDelegate() {
					@Override
					public ClassVisitor build(ClassVisitor cv) {
						IBytecodeBehavior[] currPendingBehaviors = pendingBehaviors;
						for (int a = 0; a < currPendingBehaviors.length; a++) {
							ArrayList<IBytecodeBehavior> remainingPendingBehaviors =
									new ArrayList<IBytecodeBehavior>();
							for (int b = a + 1, sizeB = currPendingBehaviors.length; b < sizeB; b++) {
								remainingPendingBehaviors.add(currPendingBehaviors[b]);
							}
							ClassVisitor newCv = currPendingBehaviors[a].extend(cv, state,
									remainingPendingBehaviors, cascadePendingBehaviors);
							currPendingBehaviors = remainingPendingBehaviors.toArray(IBytecodeBehavior.class);
							a = -1;
							if (newCv != null) {
								cv = newCv;
							}
						}
						return cv;
					}
				});
		return content;
	}

	@Override
	public void registerBytecodeBehavior(IBytecodeBehavior bytecodeBehavior) {
		bytecodeBehaviorExtensions.register(bytecodeBehavior);
		refreshSupportedEnhancements();
	}

	@Override
	public void unregisterBytecodeBehavior(IBytecodeBehavior bytecodeBehavior) {
		bytecodeBehaviorExtensions.unregister(bytecodeBehavior);
	}

	protected void refreshSupportedEnhancements() {
		writeLock.lock();
		try {
			supportedEnhancements.clear();
			for (IBytecodeBehavior bytecodeBehavior : bytecodeBehaviorExtensions.getExtensions()) {
				supportedEnhancements.addAll(bytecodeBehavior.getEnhancements());
			}
		}
		finally {
			writeLock.unlock();
		}
	}
}
