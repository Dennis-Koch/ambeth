package com.koch.ambeth.cache.collections;

/*-
 * #%L
 * jambeth-cache
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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.ioc.accessor.IAccessorTypeProvider;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.bytecode.IBytecodeEnhancer;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.collections.Tuple2KeyHashMap;

public class CacheMapEntryTypeProvider implements ICacheMapEntryTypeProvider {
	protected static final ICacheMapEntryFactory ci = new DefaultCacheMapEntryFactory();

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired(optional = true)
	protected IBytecodeEnhancer bytecodeEnhancer;

	@Autowired
	protected IAccessorTypeProvider accessorTypeProvider;

	protected final Tuple2KeyHashMap<Class<?>, Byte, ICacheMapEntryFactory> typeToConstructorMap =
			new Tuple2KeyHashMap<>();

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public ICacheMapEntryFactory getCacheMapEntryType(Class<?> entityType, byte idIndex) {
		if (bytecodeEnhancer == null) {
			return ci;
		}
		ICacheMapEntryFactory factory = typeToConstructorMap.get(entityType, Byte.valueOf(idIndex));
		if (factory != null) {
			return factory;
		}
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			// concurrent thread might have been faster
			factory = typeToConstructorMap.get(entityType, Byte.valueOf(idIndex));
			if (factory != null) {
				return factory;
			}
			Thread currentThread = Thread.currentThread();
			ClassLoader oldClassLoader = currentThread.getContextClassLoader();
			currentThread.setContextClassLoader(entityType.getClassLoader());
			try {
				Class<?> enhancedType = bytecodeEnhancer.getEnhancedType(CacheMapEntry.class,
						new CacheMapEntryEnhancementHint(entityType, idIndex));
				if (enhancedType == CacheMapEntry.class) {
					// Nothing has been enhanced
					factory = ci;
				}
				else {
					factory =
							accessorTypeProvider.getConstructorType(ICacheMapEntryFactory.class, enhancedType);
				}
			}
			catch (Throwable e) {
				if (log.isWarnEnabled()) {
					log.warn(e);
				}
				// something serious happened during enhancement: continue with a fallback
				factory = ci;
			}
			finally {
				currentThread.setContextClassLoader(oldClassLoader);
			}
			typeToConstructorMap.put(entityType, Byte.valueOf(idIndex), factory);
			return factory;
		}
		finally {
			writeLock.unlock();
		}
	}
}
