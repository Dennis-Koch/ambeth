package com.koch.ambeth.ioc.proxy;

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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.proxy.Factory;

public class CgLibUtil implements ICgLibUtil, IDisposableBean {
	public static class ClassReference extends WeakReference<Class<?>> {
		private final String name;

		public ClassReference(Class<?> referent, ReferenceQueue<Class<?>> q, String name) {
			super(referent, q);
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	protected final HashMap<String, Boolean> typeToEnhancedMap = new HashMap<>();

	protected final HashMap<String, ClassReference> typeToOriginalMap =
			new HashMap<>();

	protected final ReferenceQueue<Class<?>> classQueue = new ReferenceQueue<>();

	protected final Lock tteLock = new ReentrantLock();

	@Override
	public void destroy() throws Throwable {
		tteLock.lock();
		try {
			typeToEnhancedMap.clear();
			typeToOriginalMap.clear();
			checkForCleanup();
		}
		finally {
			tteLock.unlock();
		}
	}

	@Override
	public boolean isEnhanced(Class<?> enhancedClass) {
		tteLock.lock();
		try {
			String className = enhancedClass.getName();
			Boolean enhanced = typeToEnhancedMap.get(className);
			if (enhanced == null) {

				enhanced =
						Boolean.valueOf(Factory.class.isAssignableFrom(enhancedClass) || Proxy.isProxyClass(enhancedClass)
						// || ProxyObject.class.isAssignableFrom(enhancedClass)
						);
				typeToEnhancedMap.put(className, enhanced);
			}
			return enhanced.booleanValue();
		}
		finally {
			tteLock.unlock();
		}
	}

	@Override
	public Class<?> getOriginalClass(Class<?> enhancedClass) {
		tteLock.lock();
		try {
			String className = enhancedClass.getName();
			ClassReference originalR = typeToOriginalMap.get(className);
			Class<?> original = null;
			if (originalR != null) {
				original = originalR.get();
			}
			if (original == null) {
				original = enhancedClass;
				while (isEnhanced(original)) {
					Class<?> superClass = original.getSuperclass();
					if (Object.class.equals(superClass)) {
						original = original.getInterfaces()[0];
						break;
					}
					original = original.getSuperclass();
				}
				typeToOriginalMap.put(className, new ClassReference(original, classQueue, className));
			}
			checkForCleanup();
			return original;
		}
		finally {
			tteLock.unlock();
		}
	}

	@Override
	public Class<?>[] getAllInterfaces(Object obj, Class<?>... additional) {
		ISet<Class<?>> interfaceSet = new HashSet<>();
		Class<?> currType = obj.getClass();
		while (currType != null) {
			Class<?>[] interfaces = currType.getInterfaces();
			for (int a = interfaces.length; a-- > 0;) {
				interfaceSet.add(interfaces[a]);
			}
			currType = currType.getSuperclass();
		}
		for (int a = additional.length; a-- > 0;) {
			interfaceSet.add(additional[a]);
		}
		return interfaceSet.toArray(new Class<?>[interfaceSet.size()]);
	}

	protected void checkForCleanup() {
		ClassReference classR;
		while ((classR = (ClassReference) classQueue.poll()) != null) {
			String name = classR.getName();
			typeToEnhancedMap.remove(name);
			typeToOriginalMap.remove(name);
		}
	}
}
