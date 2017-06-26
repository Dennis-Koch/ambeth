package com.koch.ambeth.merge;

import java.io.Writer;

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

import java.util.List;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.metadata.MemberTypeProvider;
import com.koch.ambeth.merge.service.IMergeService;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.Lock;
import com.koch.ambeth.util.LockState;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class EntityMetaDataClient implements IEntityMetaDataProvider {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICache cache;

	@Autowired
	protected IMergeService mergeService;

	@Autowired
	protected IProxyHelper proxyHelper;

	@Override
	public IEntityMetaData getMetaData(Class<?> entityType) {
		return getMetaData(entityType, false);
	}

	@Override
	public IEntityMetaData getMetaData(Class<?> entityType, boolean tryOnly) {
		ArrayList<Class<?>> entityTypes = new ArrayList<>(1);
		entityTypes.add(entityType);
		IList<IEntityMetaData> metaData = getMetaData(entityTypes);
		if (!metaData.isEmpty()) {
			return metaData.get(0);
		}
		if (tryOnly) {
			return null;
		}
		throw new IllegalArgumentException(
				"No metadata found for entity of type " + entityType.getName());
	}

	@Override
	public IList<IEntityMetaData> getMetaData(List<Class<?>> entityTypes) {
		ArrayList<Class<?>> realEntityTypes = new ArrayList<>(entityTypes.size());
		for (Class<?> entityType : entityTypes) {
			realEntityTypes.add(proxyHelper.getRealType(entityType));
		}
		ICache cache = this.cache.getCurrentCache();
		Lock readLock = cache != null ? cache.getReadLock() : null;
		LockState lockState = readLock != null ? readLock.releaseAllLocks() : null;
		try {
			List<IEntityMetaData> serviceResult = mergeService.getMetaData(realEntityTypes);
			ArrayList<IEntityMetaData> result = new ArrayList<>();
			result.addAll(serviceResult);
			return result;
		}
		finally {
			if (readLock != null) {
				readLock.reacquireLocks(lockState);
			}
		}
	}

	@Override
	public IList<Class<?>> findMappableEntityTypes() {
		throw new UnsupportedOperationException(
				"This method is not supported by the EMD client stub. Please use a stateful EMD instance like caching");
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(Class<?> valueType) {
		return mergeService.getValueObjectConfig(valueType);
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(String xmlTypeName) {
		throw new UnsupportedOperationException(
				"This method is not supported by the EMD client stub. Please use a stateful EMD instance like caching");
	}

	@Override
	public List<Class<?>> getValueObjectTypesByEntityType(Class<?> entityType) {
		throw new UnsupportedOperationException(
				"This method is not supported by the EMD client stub. Please use a stateful EMD instance like caching");
	}

	@Override
	public Class<?>[] getEntityPersistOrder() {
		return MemberTypeProvider.EMPTY_TYPES;
	}

	@Override
	public void toDotGraph(Writer writer) {
		String dot = mergeService.createMetaDataDOT();
		try {
			writer.write(dot);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
