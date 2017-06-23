package com.koch.ambeth.ioc.garbageproxy;

/*-
 * #%L
 * jambeth-ioc
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.accessor.AccessorClassLoader;
import com.koch.ambeth.ioc.accessor.IAccessorTypeProvider;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.collections.Tuple2KeyEntry;
import com.koch.ambeth.util.collections.Tuple2KeyHashMap;
import com.koch.ambeth.util.proxy.AbstractSimpleInterceptor;
import com.koch.ambeth.util.proxy.ClassLoaderAwareClassWriter;

public class GarbageProxyFactory implements IGarbageProxyFactory, IInitializingBean {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IAccessorTypeProvider accessorTypeProvider;

	protected final Tuple2KeyHashMap<Class<?>, Class<?>[], IGarbageProxyConstructor<?>> interfaceTypesToConstructorMap = new Tuple2KeyHashMap<Class<?>, Class<?>[], IGarbageProxyConstructor<?>>() {
		@Override
		protected boolean equalKeys(Class<?> key1, Class<?>[] key2,
				Tuple2KeyEntry<Class<?>, Class<?>[], IGarbageProxyConstructor<?>> entry) {
			return key1.equals(entry.getKey1()) && Arrays.equals(key2, entry.getKey2());
		}

		@Override
		protected int extractHash(Class<?> key1, Class<?>[] key2) {
			return (key1 != null ? key1.hashCode() : 3) ^ (key2 != null ? Arrays.hashCode(key2) : 5);
		}
	};

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(accessorTypeProvider, "accessorTypeProvider");
	}

	public void setAccessorTypeProvider(IAccessorTypeProvider accessorTypeProvider) {
		this.accessorTypeProvider = accessorTypeProvider;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <T> IGarbageProxyConstructor<T> createGarbageProxyConstructor(Class<T> interfaceType,
			Class<?>... additionalInterfaceTypes) {
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			IGarbageProxyConstructor gpContructor = interfaceTypesToConstructorMap.get(interfaceType,
					additionalInterfaceTypes);
			if (gpContructor != null) {
				return gpContructor;
			}
			Class<?> gpType = loadClass(GCProxy.class, interfaceType, additionalInterfaceTypes);
			gpContructor = accessorTypeProvider.getConstructorType(IGarbageProxyConstructor.class,
					gpType);
			interfaceTypesToConstructorMap.put(interfaceType, additionalInterfaceTypes, gpContructor);
			return gpContructor;
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public <T> T createGarbageProxy(IDisposable target, Class<T> interfaceType,
			Class<?>... additionalInterfaceTypes) {
		return createGarbageProxy(target, target, interfaceType, additionalInterfaceTypes);
	}

	@Override
	public <T> T createGarbageProxy(Object target, IDisposable disposable, Class<T> interfaceType,
			Class<?>... additionalInterfaceTypes) {
		return createGarbageProxyConstructor(interfaceType, additionalInterfaceTypes)
				.createInstance(target, disposable);
	}

	protected Class<?> loadClass(Class<?> baseType, Class<?> interfaceType,
			Class<?>[] additionalInterfaceTypes) {
		String className = interfaceType.getName() + "$" + baseType.getSimpleName() + "$"
				+ Arrays.hashCode(additionalInterfaceTypes);
		if (className.startsWith("java.")) {
			className = "ambeth." + className;
		}
		writeLock.lock();
		try {
			AccessorClassLoader loader = AccessorClassLoader.get(interfaceType);
			try {
				return loader.loadClass(className);
			}
			catch (ClassNotFoundException e) {
				return createGpType(loader, interfaceType, additionalInterfaceTypes, className);
			}
		}
		finally {
			writeLock.unlock();
		}
	}

	protected GeneratorAdapter createGA(ClassVisitor cv, int access, String name, String desc) {
		return new GeneratorAdapter(cv.visitMethod(access, name, desc, null, null), access, name, desc);
	}

	protected Class<?> createGpType(AccessorClassLoader loader, Class<?> proxyType,
			Class<?>[] additionalProxyTypes, String className) {
		String classNameInternal = className.replace('.', '/');
		Type abstractType = Type.getType(GCProxy.class);

		LinkedHashSet<Class<?>> interfaceClasses = new LinkedHashSet<>();
		ArrayList<Type> interfaceTypes = new ArrayList<>();
		ArrayList<String> interfaceNames = new ArrayList<>();
		if (interfaceClasses.add(proxyType)) {
			interfaceTypes.add(Type.getType(proxyType));
		}
		for (Class<?> additionalProxyType : additionalProxyTypes) {
			if (interfaceClasses.add(additionalProxyType)) {
				interfaceTypes.add(Type.getType(additionalProxyType));
			}
		}
		for (Type interfaceType : interfaceTypes) {
			interfaceNames.add(interfaceType.getInternalName());
		}

		ClassWriter cw = new ClassLoaderAwareClassWriter(ClassWriter.COMPUTE_MAXS, loader);

		ClassVisitor visitor = cw;

		// comment this in to add bytecode output for eased debugging (together with commented code at
		// the end)
		// StringWriter sw = new StringWriter();
		// visitor = new TraceClassVisitor(visitor, new PrintWriter(sw));

		visitor.visit(Opcodes.V1_1, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, classNameInternal, null,
				abstractType.getInternalName(), interfaceNames.toArray(String.class));
		{
			Method method = Method.getMethod(
					"void <init> (" + Object.class.getName() + "," + IDisposable.class.getName() + ")");
			GeneratorAdapter mv = createGA(visitor, Opcodes.ACC_PUBLIC, method.getName(),
					method.getDescriptor());
			mv.loadThis();
			mv.loadArgs();
			mv.invokeConstructor(abstractType, method);
			mv.returnValue();
			mv.endMethod();
		}
		{
			Method method = Method.getMethod("void <init> (" + IDisposable.class.getName() + ")");
			GeneratorAdapter mv = createGA(visitor, Opcodes.ACC_PUBLIC, method.getName(),
					method.getDescriptor());
			mv.loadThis();
			mv.loadArgs();
			mv.invokeConstructor(abstractType, method);
			mv.returnValue();
			mv.endMethod();
		}
		Method targetMethod = Method.getMethod(
				ReflectUtil.getDeclaredMethod(false, GCProxy.class, Object.class, "resolveTarget"));

		Type objType = Type.getType(Object.class);

		HashSet<Method> alreadyImplementedMethods = new HashSet<>();
		for (Class<?> interfaceClass : interfaceClasses) {
			Type interfaceType = Type.getType(interfaceClass);
			java.lang.reflect.Method[] methods = interfaceClass.getMethods();
			for (java.lang.reflect.Method method : methods) {
				if (GCProxy.disposeMethod.equals(method)
						|| AbstractSimpleInterceptor.finalizeMethod.equals(method)) {
					// will remain implemented by the GCProxy class
					continue;
				}
				Method asmMethod = Method.getMethod(method);
				if (!alreadyImplementedMethods.add(asmMethod)) {
					continue;
				}
				GeneratorAdapter mv = createGA(visitor, Opcodes.ACC_PUBLIC, asmMethod.getName(),
						asmMethod.getDescriptor());
				int l_result = -1, l_target = -1;
				boolean resultCheckNeeded = isAssignableFrom(method.getReturnType(), interfaceClasses);
				if (resultCheckNeeded) {
					l_result = mv.newLocal(asmMethod.getReturnType());
					l_target = mv.newLocal(targetMethod.getReturnType());
				}
				mv.loadThis();
				mv.invokeVirtual(abstractType, targetMethod);
				if (resultCheckNeeded) {
					mv.storeLocal(l_target);
					mv.loadLocal(l_target);
				}
				mv.checkCast(interfaceType);
				mv.loadArgs();
				mv.invokeInterface(interfaceType, asmMethod);
				if (resultCheckNeeded) {
					// ensure that the GCProxy will be returned whenever we our target as a return value (e.g.
					// happens on fluent-APIs)
					// Example: ISqlQueryBuilder result =
					// ((ISqlQueryBuilder))this.resolveTarget()).or(..args..);
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

	private boolean isAssignableFrom(Class<?> returnType, ISet<Class<?>> interfaceClasses) {
		for (Class<?> interfaceClass : interfaceClasses) {
			if (returnType.isAssignableFrom(interfaceClass)) {
				return true;
			}
		}
		return false;
	}
}
