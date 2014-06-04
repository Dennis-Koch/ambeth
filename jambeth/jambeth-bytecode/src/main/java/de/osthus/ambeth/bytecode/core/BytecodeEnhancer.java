package de.osthus.ambeth.bytecode.core;

import java.io.StringWriter;
import java.io.Writer;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import de.osthus.ambeth.bytecode.IBuildVisitorDelegate;
import de.osthus.ambeth.bytecode.IBytecodeClassLoader;
import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.bytecode.IEnhancementHint;
import de.osthus.ambeth.bytecode.ITargetNameEnhancementHint;
import de.osthus.ambeth.bytecode.behavior.BytecodeBehaviorState;
import de.osthus.ambeth.bytecode.behavior.IBytecodeBehavior;
import de.osthus.ambeth.bytecode.behavior.IBytecodeBehaviorExtendable;
import de.osthus.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.SmartCopyMap;
import de.osthus.ambeth.collections.WeakSmartCopyMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.extendable.ExtendableContainer;
import de.osthus.ambeth.ioc.extendable.IExtendableContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.IEnhancedType;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.util.ParamHolder;

public class BytecodeEnhancer implements IBytecodeEnhancer, IBytecodeBehaviorExtendable
{
	public static class ValueType extends SmartCopyMap<IEnhancementHint, Reference<Class<?>>>
	{
		private volatile int changeCount = 0;

		public void addChangeCount()
		{
			changeCount++;
		}

		public int getChangeCount()
		{
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

	protected final WeakSmartCopyMap<Class<?>, ValueType> typeToExtendedType = new WeakSmartCopyMap<Class<?>, ValueType>();

	protected final WeakSmartCopyMap<Class<?>, Reference<Class<?>>> extendedTypeToType = new WeakSmartCopyMap<Class<?>, Reference<Class<?>>>();

	protected final HashSet<Class<?>> supportedEnhancements = new HashSet<Class<?>>(0.5f);

	protected final Lock writeLock = new ReentrantLock();

	protected final IExtendableContainer<IBytecodeBehavior> bytecodeBehaviorExtensions = new ExtendableContainer<IBytecodeBehavior>(IBytecodeBehavior.class,
			"bytecodeBehavior");

	public BytecodeEnhancer()
	{
		extendedTypeToType.setAutoCleanupReference(true);
	}

	@Override
	public boolean supportsEnhancement(Class<?> enhancementType)
	{
		writeLock.lock();
		try
		{
			return supportedEnhancements.contains(enhancementType);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public boolean isEnhancedType(Class<?> entityType)
	{
		return IEnhancedType.class.isAssignableFrom(entityType);
	}

	@Override
	public Class<?> getBaseType(Class<?> enhancedType)
	{
		if (!isEnhancedType(enhancedType))
		{
			return null;
		}
		Reference<Class<?>> typeR = extendedTypeToType.get(enhancedType);
		if (typeR == null)
		{
			throw new IllegalStateException("Must never happen");
		}
		Class<?> type = typeR.get();
		if (type == null)
		{
			throw new IllegalStateException("Must never happen");
		}
		return type;
	}

	protected Class<?> getEnhancedTypeIntern(Class<?> entityType, IEnhancementHint bytecodeEnhancementContext)
	{
		ValueType valueType = typeToExtendedType.get(entityType);
		if (valueType == null)
		{
			return null;
		}
		Reference<Class<?>> extendedTypeR = valueType.get(bytecodeEnhancementContext);
		if (extendedTypeR == null)
		{
			return null;
		}
		return extendedTypeR.get();
	}

	@Override
	public Class<?> getEnhancedType(Class<?> typeToEnhance, IEnhancementHint hint)
	{
		ITargetNameEnhancementHint targetNameHint = hint.unwrap(ITargetNameEnhancementHint.class);
		if (targetNameHint == null && hint instanceof ITargetNameEnhancementHint)
		{
			targetNameHint = (ITargetNameEnhancementHint) hint;
		}
		String newTypeNamePrefix = typeToEnhance.getName();
		if (targetNameHint != null)
		{
			newTypeNamePrefix = targetNameHint.getTargetName(typeToEnhance);
		}
		return getEnhancedType(typeToEnhance, newTypeNamePrefix, hint);
	}

	@Override
	public Class<?> getEnhancedType(Class<?> typeToEnhance, String newTypeNamePrefix, IEnhancementHint hint)
	{
		Class<?> extendedType = getEnhancedTypeIntern(typeToEnhance, hint);
		if (extendedType != null)
		{
			return extendedType;
		}
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			// Concurrent thread may have been faster
			extendedType = getEnhancedTypeIntern(typeToEnhance, hint);
			if (extendedType != null)
			{
				return extendedType;
			}
			if (log.isInfoEnabled())
			{
				log.info("Enhancing " + typeToEnhance + " with hint: " + hint);
			}
			ValueType valueType = typeToExtendedType.get(typeToEnhance);
			if (valueType == null)
			{
				valueType = new ValueType();
				typeToExtendedType.put(typeToEnhance, valueType);
			}
			else
			{
				valueType.addChangeCount();
				newTypeNamePrefix += "_O" + valueType.getChangeCount();
			}
			ArrayList<IBytecodeBehavior> pendingBehaviors = new ArrayList<IBytecodeBehavior>();

			IBytecodeBehavior[] extensions = bytecodeBehaviorExtensions.getExtensions();
			pendingBehaviors.addAll(extensions);

			Class<?> enhancedEntityType;
			if (pendingBehaviors.size() > 0)
			{
				enhancedEntityType = enhanceTypeIntern(typeToEnhance, newTypeNamePrefix, pendingBehaviors, hint);
			}
			else
			{
				enhancedEntityType = typeToEnhance;
			}
			Reference<Class<?>> entityTypeR = typeToExtendedType.getWeakReferenceEntry(typeToEnhance);
			if (entityTypeR == null)
			{
				throw new IllegalStateException("Must never happen");
			}
			WeakReference<Class<?>> enhancedEntityTypeR = new WeakReference<Class<?>>(enhancedEntityType);
			valueType.put(hint, enhancedEntityTypeR);
			extendedTypeToType.put(enhancedEntityType, entityTypeR);
			if (log.isDebugEnabled())
			{
				log.debug(bytecodeClassLoader.toPrintableBytecode(enhancedEntityType));
			}
			return enhancedEntityType;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@SuppressWarnings("resource")
	protected Class<?> enhanceTypeIntern(Class<?> originalType, String newTypeNamePrefix, final IList<IBytecodeBehavior> pendingBehaviors,
			final IEnhancementHint hint)
	{
		if (pendingBehaviors.size() == 0)
		{
			return originalType;
		}
		Reference<Class<?>> entityTypeR = typeToExtendedType.getWeakReferenceEntry(originalType);
		if (entityTypeR == null)
		{
			throw new IllegalStateException("Must never happen");
		}
		newTypeNamePrefix = newTypeNamePrefix.replaceAll(Pattern.quote("."), "/");
		final StringWriter sw = new StringWriter();
		try
		{
			Class<?> currentType = originalType;
			if (currentType.isInterface())
			{
				currentType = Object.class;
			}
			for (int a = 0, size = pendingBehaviors.size(); a < size; a++)
			{
				Class<?> newCurrentType = pendingBehaviors.get(a).getTypeToExtendFrom(originalType, currentType, hint);
				if (newCurrentType != null)
				{
					currentType = newCurrentType;
				}
			}
			int iterationCount = 0;
			byte[] currentContent = bytecodeClassLoader.readTypeAsBinary(currentType);
			while (pendingBehaviors.size() > 0)
			{
				iterationCount++;

				Type newTypeHandle = Type.getObjectType(newTypeNamePrefix + "$A" + iterationCount);

				final IBytecodeBehavior[] currentPendingBehaviors = pendingBehaviors.toArray(IBytecodeBehavior.class);
				pendingBehaviors.clear();
				final BytecodeEnhancer This = this;
				final byte[] fCurrentContent = currentContent;

				final ParamHolder<BytecodeBehaviorState> acquiredState = new ParamHolder<BytecodeBehaviorState>();
				byte[] newContent = BytecodeBehaviorState.setState(originalType, currentType, newTypeHandle, beanContext, hint,
						new IResultingBackgroundWorkerDelegate<byte[]>()
						{
							@Override
							public byte[] invoke() throws Throwable
							{
								acquiredState.setValue((BytecodeBehaviorState) BytecodeBehaviorState.getState());
								return This.executePendingBehaviors(fCurrentContent, sw, currentPendingBehaviors, pendingBehaviors);
							}
						});
				if (newContent == null)
				{
					if (pendingBehaviors.size() > 0)
					{
						// "fix" the iterationCount to have a consistent class name hierarchy
						iterationCount--;
						continue;
					}
					return currentType;
				}
				Class<?> newType = bytecodeClassLoader.loadClass(newTypeHandle.getInternalName(), newContent);
				extendedTypeToType.put(newType, entityTypeR);
				acquiredState.getValue().postProcessCreatedType(newType);
				currentContent = newContent;
				currentType = newType;
			}
			return currentType;
		}
		catch (Throwable e)
		{
			String classByteCode = sw.toString();
			if (classByteCode.length() > 0)
			{
				throw RuntimeExceptionUtil.mask(e, "Bytecode:\n" + classByteCode);
			}
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	public byte[] executePendingBehaviors(byte[] currentContent, Writer sw, final IBytecodeBehavior[] pendingBehaviors,
			final List<IBytecodeBehavior> cascadePendingBehaviors) throws Throwable
	{
		final IBytecodeBehaviorState state = BytecodeBehaviorState.getState();
		byte[] content = bytecodeClassLoader.buildTypeFromParent(state.getNewType().getInternalName(), currentContent, sw, new IBuildVisitorDelegate()
		{
			@Override
			public ClassVisitor build(ClassVisitor cv)
			{
				IBytecodeBehavior[] currPendingBehaviors = pendingBehaviors;
				for (int a = 0; a < currPendingBehaviors.length; a++)
				{
					ArrayList<IBytecodeBehavior> remainingPendingBehaviors = new ArrayList<IBytecodeBehavior>();
					for (int b = a + 1, sizeB = currPendingBehaviors.length; b < sizeB; b++)
					{
						remainingPendingBehaviors.add(currPendingBehaviors[b]);
					}
					ClassVisitor newCv = currPendingBehaviors[a].extend(cv, state, remainingPendingBehaviors, cascadePendingBehaviors);
					currPendingBehaviors = remainingPendingBehaviors.toArray(IBytecodeBehavior.class);
					a = -1;
					if (newCv != null)
					{
						cv = newCv;
					}
				}
				return cv;
			}
		});
		return content;
	}

	@Override
	public void registerBytecodeBehavior(IBytecodeBehavior bytecodeBehavior)
	{
		bytecodeBehaviorExtensions.register(bytecodeBehavior);
		refreshSupportedEnhancements();
	}

	@Override
	public void unregisterBytecodeBehavior(IBytecodeBehavior bytecodeBehavior)
	{
		bytecodeBehaviorExtensions.unregister(bytecodeBehavior);
	}

	protected void refreshSupportedEnhancements()
	{
		writeLock.lock();
		try
		{
			supportedEnhancements.clear();
			for (IBytecodeBehavior bytecodeBehavior : bytecodeBehaviorExtensions.getExtensions())
			{
				supportedEnhancements.addAll(bytecodeBehavior.getEnhancements());
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}
}