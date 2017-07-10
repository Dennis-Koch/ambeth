package com.koch.ambeth.merge;

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
import java.util.Map;
import java.util.concurrent.locks.Lock;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.extendable.IMapExtendableContainer;
import com.koch.ambeth.ioc.extendable.MapExtendableContainer;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.SmartCopyMap;

public class ValueObjectMap extends SmartCopyMap<Class<?>, List<Class<?>>>
		implements IMapExtendableContainer<Class<?>, IValueObjectConfig> {
	protected final MapExtendableContainer<Class<?>, IValueObjectConfig> typeToValueObjectConfig = new MapExtendableContainer<>(
			"configuration", "value object class");

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Override
	public IList<IValueObjectConfig> getExtensions(Class<?> key) {
		return typeToValueObjectConfig.getExtensions(key);
	}

	@Override
	public ILinkedMap<Class<?>, IValueObjectConfig> getExtensions() {
		return typeToValueObjectConfig.getExtensions();
	}

	public IList<Class<?>> getValueObjectTypesByEntityType(Class<?> entityType) {
		List<Class<?>> valueObjectTypes = get(entityType);
		if (valueObjectTypes == null) {
			// Check if the entityType is really an entity type
			if (entityMetaDataProvider.getMetaData(entityType, true) == null) {
				throw new IllegalStateException("'" + entityType + "' is no valid entity type");
			}
			return EmptyList.getInstance();
		}
		ArrayList<Class<?>> resultList = new ArrayList<>(valueObjectTypes.size());
		for (int a = 0, size = valueObjectTypes.size(); a < size; a++) {
			Class<?> valueObjectType = valueObjectTypes.get(a);
			resultList.add(valueObjectType);
		}
		return resultList;
	}

	@Override
	public IValueObjectConfig getExtension(Class<?> key) {
		return typeToValueObjectConfig.getExtension(key);
	}

	@Override
	public void getExtensions(Map<Class<?>, IValueObjectConfig> targetExtensionMap) {
		typeToValueObjectConfig.getExtensions(targetExtensionMap);
	}

	@Override
	public void register(IValueObjectConfig config, Class<?> key) {
		Class<?> entityType = config.getEntityType();
		Class<?> valueType = config.getValueType();

		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			typeToValueObjectConfig.register(config, valueType);

			// Clone list because of SmartCopy behavior
			List<Class<?>> valueObjectTypes = get(entityType);
			if (valueObjectTypes == null) {
				valueObjectTypes = new ArrayList<>(1);
			}
			else {
				valueObjectTypes = new ArrayList<>(valueObjectTypes);
			}
			valueObjectTypes.add(valueType);
			put(entityType, valueObjectTypes);
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public void unregister(IValueObjectConfig config, Class<?> key) {
		Class<?> entityType = config.getEntityType();
		Class<?> valueType = config.getValueType();

		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			typeToValueObjectConfig.unregister(config, valueType);
			List<Class<?>> valueObjectTypes = get(entityType);
			valueObjectTypes.remove(valueType);
			if (valueObjectTypes.isEmpty()) {
				remove(entityType);
			}
		}
		finally {
			writeLock.unlock();
		}
	}
}
