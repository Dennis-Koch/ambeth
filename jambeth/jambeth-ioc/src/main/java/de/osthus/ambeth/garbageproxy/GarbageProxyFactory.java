package de.osthus.ambeth.garbageproxy;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.accessor.AccessorClassLoader;
import de.osthus.ambeth.accessor.IAccessorTypeProvider;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.Tuple2KeyEntry;
import de.osthus.ambeth.collections.Tuple2KeyHashMap;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.AbstractSimpleInterceptor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassWriter;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Label;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.repackaged.org.objectweb.asm.commons.GeneratorAdapter;
import de.osthus.ambeth.repackaged.org.objectweb.asm.commons.Method;
import de.osthus.ambeth.util.IDisposable;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.ReflectUtil;

public class GarbageProxyFactory implements IGarbageProxyFactory, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IAccessorTypeProvider accessorTypeProvider;

	protected final Tuple2KeyHashMap<Class<?>, Class<?>[], IGarbageProxyConstructor<?>> interfaceTypesToConstructorMap = new Tuple2KeyHashMap<Class<?>, Class<?>[], IGarbageProxyConstructor<?>>()
	{
		@Override
		protected boolean equalKeys(Class<?> key1, Class<?>[] key2, Tuple2KeyEntry<Class<?>, Class<?>[], IGarbageProxyConstructor<?>> entry)
		{
			return key1.equals(entry.getKey1()) && Arrays.equals(key2, entry.getKey2());
		}

		@Override
		protected int extractHash(Class<?> key1, Class<?>[] key2)
		{
			return (key1 != null ? key1.hashCode() : 3) ^ (key2 != null ? Arrays.hashCode(key2) : 5);
		}
	};

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(accessorTypeProvider, "accessorTypeProvider");
	}

	public void setAccessorTypeProvider(IAccessorTypeProvider accessorTypeProvider)
	{
		this.accessorTypeProvider = accessorTypeProvider;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <T> IGarbageProxyConstructor<T> createGarbageProxyConstructor(Class<T> interfaceType, Class<?>... additionalInterfaceTypes)
	{
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			IGarbageProxyConstructor gpContructor = interfaceTypesToConstructorMap.get(interfaceType, additionalInterfaceTypes);
			if (gpContructor != null)
			{
				return gpContructor;
			}
			Class<?> gpType = loadClass(GCProxy.class, interfaceType, additionalInterfaceTypes);
			gpContructor = accessorTypeProvider.getConstructorType(IGarbageProxyConstructor.class, gpType);
			interfaceTypesToConstructorMap.put(interfaceType, additionalInterfaceTypes, gpContructor);
			return gpContructor;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public <T> T createGarbageProxy(IDisposable target, Class<T> interfaceType, Class<?>... additionalInterfaceTypes)
	{
		return createGarbageProxy(target, target, interfaceType, additionalInterfaceTypes);
	}

	@Override
	public <T> T createGarbageProxy(Object target, IDisposable disposable, Class<T> interfaceType, Class<?>... additionalInterfaceTypes)
	{
		return createGarbageProxyConstructor(interfaceType, additionalInterfaceTypes).createInstance(target, disposable);
	}

	protected Class<?> loadClass(Class<?> baseType, Class<?> interfaceType, Class<?>[] additionalInterfaceTypes)
	{
		String className = interfaceType.getName() + "$" + baseType.getSimpleName() + "$" + Arrays.hashCode(additionalInterfaceTypes);
		if (className.startsWith("java."))
		{
			className = "ambeth." + className;
		}
		writeLock.lock();
		try
		{
			AccessorClassLoader loader = AccessorClassLoader.get(interfaceType);
			try
			{
				return loader.loadClass(className);
			}
			catch (ClassNotFoundException e)
			{
				return createGpType(loader, interfaceType, additionalInterfaceTypes, className);
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

	protected Class<?> createGpType(AccessorClassLoader loader, Class<?> proxyType, Class<?>[] additionalProxyTypes, String className)
	{
		String classNameInternal = className.replace('.', '/');
		Type abstractType = Type.getType(GCProxy.class);

		ArrayList<Class<?>> interfaceClasses = new ArrayList<Class<?>>();
		ArrayList<Type> interfaceTypes = new ArrayList<Type>();
		ArrayList<String> interfaceNames = new ArrayList<String>();
		interfaceTypes.add(Type.getType(proxyType));
		interfaceClasses.add(proxyType);
		for (Class<?> additionalProxyType : additionalProxyTypes)
		{
			interfaceTypes.add(Type.getType(additionalProxyType));
			interfaceClasses.add(additionalProxyType);
		}
		for (Type interfaceType : interfaceTypes)
		{
			interfaceNames.add(interfaceType.getInternalName());
		}

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

		ClassVisitor visitor = cw;

		// comment this in to add bytecode output for eased debugging (together with commented code at the end)
		// StringWriter sw = new StringWriter();
		// visitor = new TraceClassVisitor(visitor, new PrintWriter(sw));

		visitor.visit(Opcodes.V1_1, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, classNameInternal, null, abstractType.getInternalName(),
				interfaceNames.toArray(String.class));
		{
			Method method = Method.getMethod("void <init> (" + Object.class.getName() + "," + IDisposable.class.getName() + ")");
			GeneratorAdapter mv = createGA(visitor, Opcodes.ACC_PUBLIC, method.getName(), method.getDescriptor());
			mv.loadThis();
			mv.loadArgs();
			mv.invokeConstructor(abstractType, method);
			mv.returnValue();
			mv.endMethod();
		}
		{
			Method method = Method.getMethod("void <init> (" + IDisposable.class.getName() + ")");
			GeneratorAdapter mv = createGA(visitor, Opcodes.ACC_PUBLIC, method.getName(), method.getDescriptor());
			mv.loadThis();
			mv.loadArgs();
			mv.invokeConstructor(abstractType, method);
			mv.returnValue();
			mv.endMethod();
		}
		Method targetMethod = Method.getMethod(ReflectUtil.getDeclaredMethod(false, GCProxy.class, Object.class, "resolveTarget"));

		Type objType = Type.getType(Object.class);

		HashSet<Method> alreadyImplementedMethods = new HashSet<Method>();
		for (Class<?> interfaceClass : interfaceClasses)
		{
			Type interfaceType = Type.getType(interfaceClass);
			java.lang.reflect.Method[] methods = interfaceClass.getMethods();
			for (java.lang.reflect.Method method : methods)
			{
				if (GCProxy.disposeMethod.equals(method) || AbstractSimpleInterceptor.finalizeMethod.equals(method))
				{
					// will remain implemented by the GCProxy class
					continue;
				}
				Method asmMethod = Method.getMethod(method);
				if (!alreadyImplementedMethods.add(asmMethod))
				{
					continue;
				}
				GeneratorAdapter mv = createGA(visitor, Opcodes.ACC_PUBLIC, asmMethod.getName(), asmMethod.getDescriptor());
				int l_result = -1, l_target = -1;
				boolean resultCheckNeeded = isAssignableFrom(method.getReturnType(), interfaceClasses);
				if (resultCheckNeeded)
				{
					l_result = mv.newLocal(asmMethod.getReturnType());
					l_target = mv.newLocal(targetMethod.getReturnType());
				}
				mv.loadThis();
				mv.invokeVirtual(abstractType, targetMethod);
				if (resultCheckNeeded)
				{
					mv.storeLocal(l_target);
					mv.loadLocal(l_target);
				}
				mv.checkCast(interfaceType);
				mv.loadArgs();
				mv.invokeInterface(interfaceType, asmMethod);
				if (resultCheckNeeded)
				{
					// ensure that the GCProxy will be returned whenever we our target as a return value (e.g. happens on fluent-APIs)
					// Example: ISqlQueryBuilder result = ((ISqlQueryBuilder))this.resolveTarget()).or(..args..);
					mv.storeLocal(l_result);

					Label label_returnThis = mv.newLabel();

					// if (result == resolveTarget())
					mv.loadLocal(l_target);
					mv.loadLocal(l_result);
					mv.ifCmp(objType, GeneratorAdapter.EQ, label_returnThis);

					// else return result;
					mv.loadLocal(l_result);
					mv.returnValue();

					// return this;
					mv.mark(label_returnThis);
					mv.loadThis();
				}
				mv.returnValue();
				mv.endMethod();
			}
		}
		visitor.visitEnd();
		byte[] data = cw.toByteArray();

		// comment this in to add bytecode output for eased debugging
		// String string = sw.toString();
		// System.out.println(string);
		return loader.defineClass(className, data);
	}

	private boolean isAssignableFrom(Class<?> returnType, List<Class<?>> interfaceClasses)
	{
		for (int a = interfaceClasses.size(); a-- > 0;)
		{
			Class<?> interfaceClass = interfaceClasses.get(a);
			if (returnType.isAssignableFrom(interfaceClass))
			{
				return true;
			}
		}
		return false;
	}
}
