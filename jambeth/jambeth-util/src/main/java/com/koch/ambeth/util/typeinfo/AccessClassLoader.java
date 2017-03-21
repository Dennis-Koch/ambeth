package com.koch.ambeth.util.typeinfo;

/*-
 * #%L
 * jambeth-util
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.repackaged.com.esotericsoftware.reflectasm.FieldAccess;
import com.koch.ambeth.repackaged.com.esotericsoftware.reflectasm.MethodAccess;
import com.koch.ambeth.util.collections.WeakHashMap;

public class AccessClassLoader extends ClassLoader
{
	private static final WeakHashMap<ClassLoader, Reference<AccessClassLoader>> parentToAccessClassLoaderMap = new WeakHashMap<ClassLoader, Reference<AccessClassLoader>>(
			0.5f);

	private static final Lock writeLock = new ReentrantLock();

	protected static Reference<AccessClassLoader> noParentAccessClassLoaderR;

	static AccessClassLoader get(Class<?> type)
	{
		ClassLoader parent = type.getClassLoader();
		writeLock.lock();
		try
		{
			Reference<AccessClassLoader> accessClassLoaderR = parent != null ? parentToAccessClassLoaderMap.get(parent) : noParentAccessClassLoaderR;

			AccessClassLoader accessClassLoader = null;
			if (accessClassLoaderR != null)
			{
				accessClassLoader = accessClassLoaderR.get();
			}
			if (accessClassLoader == null)
			{
				accessClassLoader = new AccessClassLoader(parent);
				accessClassLoaderR = new WeakReference<AccessClassLoader>(accessClassLoader);
				if (parent != null)
				{
					parentToAccessClassLoaderMap.put(parent, accessClassLoaderR);
				}
				else
				{
					noParentAccessClassLoaderR = accessClassLoaderR;
				}
			}
			return accessClassLoader;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	private AccessClassLoader(ClassLoader parent)
	{
		super(parent);
	}

	@Override
	protected synchronized java.lang.Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
	{
		// These classes come from the classloader that loaded AccessClassLoader.
		if (name.equals(FieldAccess.class.getName()))
		{
			return FieldAccess.class;
		}
		if (name.equals(MethodAccess.class.getName()))
		{
			return MethodAccess.class;
		}
		if (name.equals(FastConstructorAccess.class.getName()))
		{
			return FastConstructorAccess.class;
		}
		// All other classes come from the classloader that loaded the type we are accessing.
		return super.loadClass(name, resolve);
	}

	Class<?> defineClass(String name, byte[] bytes) throws ClassFormatError
	{
		try
		{
			// Attempt to load the access class in the same loader, which makes protected and default access members accessible.
			Method method = ClassLoader.class.getDeclaredMethod("defineClass", new Class[] { String.class, byte[].class, int.class, int.class });
			method.setAccessible(true);
			return (Class<?>) method.invoke(getParent(), new Object[] { name, bytes, Integer.valueOf(0), Integer.valueOf(bytes.length) });
		}
		catch (Exception ignored)
		{
		}
		return defineClass(name, bytes, 0, bytes.length);
	}
}
