package de.osthus.ambeth.accessor;

import static de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.Tuple2KeyEntry;
import de.osthus.ambeth.collections.Tuple2KeyHashMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassWriter;
import de.osthus.ambeth.repackaged.org.objectweb.asm.MethodVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.repackaged.org.objectweb.asm.commons.GeneratorAdapter;
import de.osthus.ambeth.repackaged.org.objectweb.asm.commons.Method;
import de.osthus.ambeth.util.ReflectUtil;
import de.osthus.ambeth.util.TypeUtil;

public class AccessorTypeProvider implements IAccessorTypeProvider, IInitializingBean
{
	protected static final Constructor<? extends AbstractAccessor> ci;

	static
	{
		try
		{
			ci = DefaultAccessor.class.getConstructor(Class.class, String.class, Class.class);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final Tuple2KeyHashMap<Class<?>, String, Reference<AbstractAccessor>> typeToAccessorMap = new Tuple2KeyHashMap<Class<?>, String, Reference<AbstractAccessor>>()
	{
		@Override
		protected void resize(int newCapacity)
		{
			ArrayList<Object[]> removeKeys = new ArrayList<Object[]>();
			for (Tuple2KeyEntry<Class<?>, String, Reference<AbstractAccessor>> entry : this)
			{
				if (entry.getValue().get() == null)
				{
					removeKeys.add(new Object[] { entry.getKey1(), entry.getKey2() });
				}
			}
			for (Object[] removeKey : removeKeys)
			{
				remove((Class<?>) removeKey[0], (String) removeKey[1]);
			}
			if (size() >= threshold)
			{
				super.resize(2 * table.length);
			}
		}
	};

	protected final Tuple2KeyHashMap<Class<?>, Class<?>, Object> typeWithDelegateToConstructorMap = new Tuple2KeyHashMap<Class<?>, Class<?>, Object>();

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
	}

	@Override
	public AbstractAccessor getAccessorType(Class<?> type, String propertyName, Class<?> propertyType)
	{
		Reference<AbstractAccessor> accessorR = typeToAccessorMap.get(type, propertyName);
		AbstractAccessor accessor = accessorR != null ? accessorR.get() : null;
		if (accessor != null)
		{
			return accessor;
		}
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			// concurrent thread might have been faster
			accessorR = typeToAccessorMap.get(type, propertyName);
			accessor = accessorR != null ? accessorR.get() : null;
			if (accessor != null)
			{
				return accessor;
			}
			try
			{
				Class<?> enhancedType = getAccessorTypeIntern(type, propertyName, propertyType);
				if (enhancedType != AbstractAccessor.class)
				{
					Constructor<?> constructor = enhancedType.getConstructor(Class.class, String.class);
					accessor = (AbstractAccessor) constructor.newInstance(type, propertyName);
				}
			}
			catch (Throwable e)
			{
				if (log.isWarnEnabled())
				{
					log.warn(e);
				}
			}
			if (accessor == null)
			{
				// something serious happened during enhancement: continue with a fallback
				accessor = ci.newInstance(type, propertyName, propertyType);
			}
			typeToAccessorMap.put(type, propertyName, new WeakReference<AbstractAccessor>(accessor));
			return accessor;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> V getConstructorType(Class<V> delegateType, Class<?> targetType)
	{
		Object constructorDelegate = typeWithDelegateToConstructorMap.get(targetType, delegateType);
		if (constructorDelegate != null)
		{
			return (V) constructorDelegate;
		}
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			// concurrent thread might have been faster
			constructorDelegate = typeWithDelegateToConstructorMap.get(targetType, delegateType);
			if (constructorDelegate != null)
			{
				return (V) constructorDelegate;
			}
			Class<?> enhancedType = getConstructorTypeIntern(delegateType, targetType);
			constructorDelegate = enhancedType.newInstance();
			typeWithDelegateToConstructorMap.put(targetType, delegateType, constructorDelegate);
			return (V) constructorDelegate;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected Class<?> getConstructorTypeIntern(Class<?> delegateType, Class<?> targetType)
	{
		String constructorClassName = targetType.getName() + "$FastConstructor$" + delegateType.getName();
		if (constructorClassName.startsWith("java."))
		{
			constructorClassName = "ambeth." + constructorClassName;
		}
		writeLock.lock();
		try
		{
			AccessorClassLoader loader = AccessorClassLoader.get(targetType);
			try
			{
				return loader.loadClass(constructorClassName);
			}
			catch (ClassNotFoundException ignored)
			{
				return createConstructorType(loader, constructorClassName, delegateType, targetType);
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected Class<?> getAccessorTypeIntern(Class<?> targetType, String propertyName, Class<?> propertyType)
	{
		String accessClassName = targetType.getName() + "$" + AbstractAccessor.class.getSimpleName() + "$" + propertyName;
		if (accessClassName.startsWith("java."))
		{
			accessClassName = "ambeth." + accessClassName;
		}
		writeLock.lock();
		try
		{
			AccessorClassLoader loader = AccessorClassLoader.get(targetType);
			try
			{
				return loader.loadClass(accessClassName);
			}
			catch (ClassNotFoundException ignored)
			{
				return createType(loader, accessClassName, targetType, propertyName, propertyType);
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected GeneratorAdapter createGA(ClassVisitor cv, int access, String name, String desc)
	{
		return new GeneratorAdapter(cv.visitMethod(access, name, desc, null, null), access, name, desc);
	}

	protected Class<?> createType(AccessorClassLoader loader, String accessClassName, Class<?> targetType, String propertyName, Class<?> propertyType)
	{
		if (log.isDebugEnabled())
		{
			log.debug("Creating accessor for " + targetType.getName() + "." + propertyName);
		}
		String accessClassNameInternal = accessClassName.replace('.', '/');

		Type abstractAccessorType = Type.getType(AbstractAccessor.class);
		Type objType = Type.getType(Object.class);

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		cw.visit(Opcodes.V1_1, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, accessClassNameInternal, null, abstractAccessorType.getInternalName(), null);
		{
			String desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Class.class), Type.getType(String.class));
			Method method = Method.getMethod("void <init> (Class,String)");
			GeneratorAdapter mv = createGA(cw, Opcodes.ACC_PUBLIC, method.getName(), desc);
			mv.loadThis();
			mv.loadArg(0);
			mv.loadArg(1);
			mv.invokeConstructor(abstractAccessorType, method);
			mv.returnValue();
			mv.endMethod();
		}
		java.lang.reflect.Method r_get = ReflectUtil.getDeclaredMethod(true, targetType, propertyType, "get" + propertyName, new Class<?>[0]);
		if (r_get == null)
		{
			r_get = ReflectUtil.getDeclaredMethod(true, targetType, propertyType, "is" + propertyName, new Class<?>[0]);
		}
		java.lang.reflect.Method r_set = ReflectUtil.getDeclaredMethod(true, targetType, null, "set" + propertyName, propertyType);

		{
			String desc = Type.getMethodDescriptor(Type.BOOLEAN_TYPE);
			GeneratorAdapter mv = createGA(cw, Opcodes.ACC_PUBLIC, "canRead", desc);
			mv.push(r_get != null && Modifier.isPublic(r_get.getModifiers()));
			mv.returnValue();
			mv.endMethod();
		}
		{
			String desc = Type.getMethodDescriptor(Type.BOOLEAN_TYPE);
			GeneratorAdapter mv = createGA(cw, Opcodes.ACC_PUBLIC, "canWrite", desc);
			mv.push(r_set != null && Modifier.isPublic(r_set.getModifiers()));
			mv.returnValue();
			mv.endMethod();
		}
		{
			String desc = Type.getMethodDescriptor(objType, objType);
			GeneratorAdapter mv = createGA(cw, Opcodes.ACC_PUBLIC, "getValue", desc);

			if (r_get == null)
			{
				mv.throwException(Type.getType(UnsupportedOperationException.class), "Property not readable: " + targetType.getName() + "." + propertyName);
			}
			else
			{
				Type owner = Type.getType(r_get.getDeclaringClass());
				mv.loadArg(0);
				mv.checkCast(owner);
				mv.invokeVirtual(owner, Method.getMethod(r_get));
				smartBox(mv, Type.getType(r_get.getReturnType()));
			}
			mv.returnValue();
			mv.endMethod();
		}
		{
			String desc = Type.getMethodDescriptor(Type.VOID_TYPE, objType, objType);
			GeneratorAdapter mv = createGA(cw, Opcodes.ACC_PUBLIC, "setValue", desc);

			if (r_set == null)
			{
				mv.throwException(Type.getType(UnsupportedOperationException.class), "Property not writable: " + targetType.getName() + "." + propertyName);
			}
			else
			{
				Type owner = Type.getType(r_set.getDeclaringClass());
				mv.loadArg(0);
				mv.checkCast(owner);
				mv.loadArg(1);
				Type paramType = Type.getType(r_set.getParameterTypes()[0]);
				if (!objType.equals(paramType))
				{
					if (r_set.getParameterTypes()[0].isPrimitive())
					{
						mv.unbox(paramType);
					}
					else
					{
						mv.checkCast(paramType);
					}
				}
				mv.invokeVirtual(owner, Method.getMethod(r_set));
			}
			mv.returnValue();
			mv.endMethod();
		}
		cw.visitEnd();
		byte[] data = cw.toByteArray();
		return loader.defineClass(accessClassName, data);
	}

	protected Class<?> createConstructorType(AccessorClassLoader loader, String constructorClassName, Class<?> delegateType, Class<?> targetType)
	{
		if (log.isDebugEnabled())
		{
			log.debug("Creating fast constructor handle for " + targetType.getName());
		}
		String constructorClassNameInternal = constructorClassName.replace('.', '/');

		Type delegateTypeHandle = Type.getType(delegateType);
		Type objType = Type.getType(Object.class);
		Type superType;

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		if (delegateType.isInterface())
		{
			superType = objType;
			cw.visit(Opcodes.V1_1, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, constructorClassNameInternal, null, superType.getInternalName(),
					new String[] { delegateTypeHandle.getInternalName() });
		}
		else
		{
			superType = delegateTypeHandle;
			cw.visit(Opcodes.V1_1, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, constructorClassNameInternal, null, superType.getInternalName(), null);
		}
		{
			String desc = Type.getMethodDescriptor(Type.VOID_TYPE);
			Method method = Method.getMethod("void <init> ()");
			GeneratorAdapter mv = createGA(cw, Opcodes.ACC_PUBLIC, method.getName(), desc);
			mv.loadThis();
			mv.invokeConstructor(superType, method);
			mv.returnValue();
			mv.endMethod();
		}
		java.lang.reflect.Method[] r_methods = delegateType.getMethods();

		Constructor<?>[] constructors = targetType.getConstructors();
		for (Constructor<?> constructor : constructors)
		{
			Class<?>[] constructorParams = constructor.getParameterTypes();
			java.lang.reflect.Method r_selectedMethod = null;
			for (int a = r_methods.length; a-- > 0;)
			{
				java.lang.reflect.Method r_method = r_methods[a];
				if (r_method == null)
				{
					// already handled
					continue;
				}
				if (!delegateType.isInterface() && !Modifier.isAbstract(r_method.getModifiers()))
				{
					// only handle abstract methods
					r_methods[a] = null;
					continue;
				}
				Class<?>[] methodParams = r_method.getParameterTypes();
				if (constructorParams.length != methodParams.length)
				{
					// no match
					continue;
				}
				boolean paramsEqual = true;
				for (int b = constructorParams.length; b-- > 0;)
				{
					if (!constructorParams[b].equals(methodParams[b]))
					{
						paramsEqual = false;
						break;
					}
				}
				if (!paramsEqual)
				{
					// no match
					continue;
				}
				r_methods[a] = null;
				r_selectedMethod = r_method;
				break;
			}
			if (r_selectedMethod == null)
			{
				// no delegate method found to invoke constructor
				continue;
			}
			Type[] paramTypes = TypeUtil.getClassesToTypes(r_selectedMethod.getParameterTypes());
			Method method = Method.getMethod(r_selectedMethod);
			GeneratorAdapter mv = createGA(cw, Opcodes.ACC_PUBLIC, method.getName(), method.getDescriptor());
			Type instanceType = Type.getType(constructor.getDeclaringClass());
			mv.newInstance(instanceType);
			mv.dup();
			for (int b = 0, sizeB = paramTypes.length; b < sizeB; b++)
			{
				mv.loadArg(b);
			}
			mv.invokeConstructor(instanceType, Method.getMethod(constructor));
			mv.returnValue();
			mv.endMethod();
		}
		for (java.lang.reflect.Method r_method : r_methods)
		{
			if (r_method != null)
			{
				throw new IllegalArgumentException("No matching constructor found on " + targetType.getName() + " to map on delegate method "
						+ r_method.toString());
			}
		}
		cw.visitEnd();
		byte[] data = cw.toByteArray();
		return loader.defineClass(constructorClassName, data);
	}

	public static void smartBox(MethodVisitor mv, Type unboxedType)
	{
		switch (unboxedType.getSort())
		{
			case Type.BOOLEAN:
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
				break;
			case Type.BYTE:
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
				break;
			case Type.CHAR:
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
				break;
			case Type.SHORT:
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
				break;
			case Type.INT:
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
				break;
			case Type.FLOAT:
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
				break;
			case Type.LONG:
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
				break;
			case Type.DOUBLE:
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
				break;
		}
	}
}
