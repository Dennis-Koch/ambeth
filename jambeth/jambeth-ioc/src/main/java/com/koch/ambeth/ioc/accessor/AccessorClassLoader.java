package com.koch.ambeth.ioc.accessor;

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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.repackaged.com.esotericsoftware.reflectasm.ConstructorAccess;
import com.koch.ambeth.repackaged.com.esotericsoftware.reflectasm.FieldAccess;
import com.koch.ambeth.repackaged.com.esotericsoftware.reflectasm.MethodAccess;

public class AccessorClassLoader extends ClassLoader {
	private static final ArrayList<Reference<AccessorClassLoader>> accessClassLoaders =
			new ArrayList<>();

	private static Method defineClassMethod;

	static {
		try {
			defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass",
					new Class[] {String.class, byte[].class, int.class, int.class});
			defineClassMethod.setAccessible(true);
		}
		catch (Throwable e) {
			defineClassMethod = null;
		}
	}

	private static final Lock writeLock = new ReentrantLock();

	public static AccessorClassLoader get(Class<?> type) {
		return get(type.getClassLoader());
	}

	public static AccessorClassLoader get(ClassLoader parent) {
		writeLock.lock();
		try {
			for (int i = accessClassLoaders.size(); i-- > 0;) {
				Reference<AccessorClassLoader> accessClassLoaderR = accessClassLoaders.get(i);
				AccessorClassLoader accessClassLoader = accessClassLoaderR.get();
				if (accessClassLoader == null) {
					// Current ClassLoader is invalidated
					accessClassLoaders.remove(i);
					continue;
				}
				if (accessClassLoader.getParent() == parent) {
					return accessClassLoader;
				}
			}
			AccessorClassLoader accessClassLoader = new AccessorClassLoader(parent);
			accessClassLoaders.add(new WeakReference<>(accessClassLoader));
			return accessClassLoader;
		}
		finally {
			writeLock.unlock();
		}
	}

	public static void remove(ClassLoader parent) {
		writeLock.lock();
		try {
			for (int i = accessClassLoaders.size(); i-- > 0;) {
				Reference<AccessorClassLoader> accessClassLoaderR = accessClassLoaders.get(i);
				AccessorClassLoader accessClassLoader = accessClassLoaderR.get();
				if (accessClassLoader == null) {
					// Current ClassLoader is invalidated
					accessClassLoaders.remove(i);
					continue;
				}
				if (accessClassLoader.getParent() == parent) {
					accessClassLoaders.remove(i);
				}
			}
		}
		finally {
			writeLock.unlock();
		}
	}

	private AccessorClassLoader(ClassLoader parent) {
		super(parent);
	}

	@Override
	protected java.lang.Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		writeLock.lock();
		try {
			// These classes come from the classloader that loaded AccessClassLoader.
			if (name.equals(FieldAccess.class.getName())) {
				return FieldAccess.class;
			}
			if (name.equals(MethodAccess.class.getName())) {
				return MethodAccess.class;
			}
			if (name.equals(ConstructorAccess.class.getName())) {
				return ConstructorAccess.class;
			}
			if (name.equals(AbstractAccessor.class.getName())) {
				return AbstractAccessor.class;
			}
			// All other classes come from the classloader that loaded the type we are accessing.
			return super.loadClass(name, resolve);
		}
		finally {
			writeLock.unlock();
		}
	}

	public Class<?> defineClass(String name, byte[] bytes) throws ClassFormatError {
		try {
			// Attempt to load the access class in the same loader, which makes protected and default
			// access members accessible.
			return (Class<?>) defineClassMethod.invoke(getParent(),
					new Object[] {name, bytes, Integer.valueOf(0), Integer.valueOf(bytes.length)});
		}
		catch (Exception ignored) {
			// intended blank
		}
		return defineClass(name, bytes, 0, bytes.length);
	}
}
