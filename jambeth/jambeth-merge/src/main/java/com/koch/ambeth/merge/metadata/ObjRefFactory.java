package com.koch.ambeth.merge.metadata;

/*-
 * #%L
 * jambeth-merge
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
import com.koch.ambeth.merge.cache.AbstractCacheValue;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.Tuple2KeyHashMap;

public class ObjRefFactory extends IObjRefFactory {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IAccessorTypeProvider accessorTypeProvider;

	@Autowired
	protected IBytecodeEnhancer bytecodeEnhancer;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected final Tuple2KeyHashMap<Class<?>, Integer, IPreparedObjRefFactory> constructorDelegateMap =
			new Tuple2KeyHashMap<>();

	protected final Lock writeLock = new ReentrantLock();

	protected IPreparedObjRefFactory buildDelegate(Class<?> realType, int idIndex) {
		writeLock.lock();
		try {
			IPreparedObjRefFactory objRefConstructorDelegate =
					constructorDelegateMap.get(realType, Integer.valueOf(idIndex));
			if (objRefConstructorDelegate != null) {
				return objRefConstructorDelegate;
			}
			Class<?> enhancedType = bytecodeEnhancer.getEnhancedType(Object.class,
					new ObjRefEnhancementHint(realType, idIndex));
			objRefConstructorDelegate =
					accessorTypeProvider.getConstructorType(IPreparedObjRefFactory.class, enhancedType);
			constructorDelegateMap.put(realType, Integer.valueOf(idIndex), objRefConstructorDelegate);
			return objRefConstructorDelegate;
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public IObjRef dup(IObjRef objRef) {
		IPreparedObjRefFactory objRefConstructorDelegate =
				constructorDelegateMap.get(objRef.getRealType(), Integer.valueOf(objRef.getIdNameIndex()));
		if (objRefConstructorDelegate == null) {
			objRefConstructorDelegate = buildDelegate(objRef.getRealType(), objRef.getIdNameIndex());
		}
		return objRefConstructorDelegate.createObjRef(objRef.getId(), objRef.getVersion());
	}

	@Override
	public IObjRef createObjRef(AbstractCacheValue cacheValue) {
		IPreparedObjRefFactory objRefConstructorDelegate = constructorDelegateMap
				.get(cacheValue.getEntityType(), Integer.valueOf(ObjRef.PRIMARY_KEY_INDEX));
		if (objRefConstructorDelegate == null) {
			objRefConstructorDelegate =
					buildDelegate(cacheValue.getEntityType(), ObjRef.PRIMARY_KEY_INDEX);
		}
		return objRefConstructorDelegate.createObjRef(cacheValue.getId(), cacheValue.getVersion());
	}

	@Override
	public IObjRef createObjRef(AbstractCacheValue cacheValue, int idIndex) {
		IPreparedObjRefFactory objRefConstructorDelegate =
				constructorDelegateMap.get(cacheValue.getEntityType(), Integer.valueOf(idIndex));
		if (objRefConstructorDelegate == null) {
			objRefConstructorDelegate = buildDelegate(cacheValue.getEntityType(), idIndex);
		}
		return objRefConstructorDelegate.createObjRef(cacheValue.getId(), cacheValue.getVersion());
	}

	@Override
	public IObjRef createObjRef(Class<?> entityType, int idIndex, Object id, Object version) {
		IPreparedObjRefFactory objRefConstructorDelegate =
				constructorDelegateMap.get(entityType, Integer.valueOf(idIndex));
		if (objRefConstructorDelegate == null) {
			objRefConstructorDelegate = buildDelegate(entityType, idIndex);
		}
		return objRefConstructorDelegate.createObjRef(id, version);
	}

	@Override
	public IPreparedObjRefFactory prepareObjRefFactory(Class<?> entityType, int idIndex) {
		IPreparedObjRefFactory objRefConstructorDelegate =
				constructorDelegateMap.get(entityType, Integer.valueOf(idIndex));
		if (objRefConstructorDelegate == null) {
			objRefConstructorDelegate = buildDelegate(entityType, idIndex);
		}
		return objRefConstructorDelegate;
	}
}
