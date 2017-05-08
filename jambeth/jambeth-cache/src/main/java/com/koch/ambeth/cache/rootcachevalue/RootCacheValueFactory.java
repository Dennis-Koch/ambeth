package com.koch.ambeth.cache.rootcachevalue;

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
import com.koch.ambeth.merge.bytecode.IBytecodePrinter;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.collections.HashMap;

public class RootCacheValueFactory implements IRootCacheValueFactory {
	protected static final RootCacheValueFactoryDelegate rcvFactory =
			new DefaultRootCacheValueFactoryDelegate();

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired(optional = true)
	protected IAccessorTypeProvider accessorTypeProvider;

	@Autowired(optional = true)
	protected IBytecodeEnhancer bytecodeEnhancer;

	@Autowired(optional = true)
	protected IBytecodePrinter bytecodePrinter;

	protected final HashMap<IEntityMetaData, RootCacheValueFactoryDelegate> typeToConstructorMap =
			new HashMap<>();

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public RootCacheValue createRootCacheValue(IEntityMetaData metaData) {
		RootCacheValueFactoryDelegate rootCacheValueFactory = typeToConstructorMap.get(metaData);
		if (rootCacheValueFactory != null) {
			return rootCacheValueFactory.createRootCacheValue(metaData);
		}
		if (bytecodeEnhancer == null) {
			return rcvFactory.createRootCacheValue(metaData);
		}
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			// concurrent thread might have been faster
			rootCacheValueFactory = typeToConstructorMap.get(metaData);
			if (rootCacheValueFactory == null) {
				rootCacheValueFactory = createDelegate(metaData);
			}
		}
		finally {
			writeLock.unlock();
		}
		return rootCacheValueFactory.createRootCacheValue(metaData);
	}

	protected RootCacheValueFactoryDelegate createDelegate(IEntityMetaData metaData) {
		RootCacheValueFactoryDelegate rootCacheValueFactory;
		Class<?> enhancedType = null;
		try {
			enhancedType = bytecodeEnhancer.getEnhancedType(RootCacheValue.class,
					new RootCacheValueEnhancementHint(metaData.getEntityType()));
			if (enhancedType == RootCacheValue.class) {
				// Nothing has been enhanced
				rootCacheValueFactory = rcvFactory;
			}
			else {
				rootCacheValueFactory = accessorTypeProvider
						.getConstructorType(RootCacheValueFactoryDelegate.class, enhancedType);
			}
		}
		catch (Throwable e) {
			if (log.isWarnEnabled()) {
				log.warn(bytecodePrinter.toPrintableBytecode(enhancedType), e);
			}
			// something serious happened during enhancement: continue with a fallback
			rootCacheValueFactory = rcvFactory;
		}
		typeToConstructorMap.put(metaData, rootCacheValueFactory);
		return rootCacheValueFactory;
	}
}
