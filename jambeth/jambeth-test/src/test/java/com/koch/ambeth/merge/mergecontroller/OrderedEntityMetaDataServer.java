package com.koch.ambeth.merge.mergecontroller;

import java.io.Writer;

/*-
 * #%L
 * jambeth-test
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

import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.collections.IList;

public class OrderedEntityMetaDataServer implements IEntityMetaDataProvider {
	private final Class<?>[] entityPersistOrder = { Parent.class, Child.class };

	private final IEntityMetaDataProvider entityMetaDataProvider;

	public OrderedEntityMetaDataServer(IEntityMetaDataProvider entityMetaDataProvider) {
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	@Override
	public Class<?>[] getEntityPersistOrder() {
		return entityPersistOrder;
	}

	@Override
	public IEntityMetaData getMetaData(Class<?> entityType) {
		return entityMetaDataProvider.getMetaData(entityType);
	}

	@Override
	public IEntityMetaData getMetaData(Class<?> entityType, boolean tryOnly) {
		return entityMetaDataProvider.getMetaData(entityType, tryOnly);
	}

	@Override
	public IList<IEntityMetaData> getMetaData(List<Class<?>> entityTypes) {
		return entityMetaDataProvider.getMetaData(entityTypes);
	}

	@Override
	public IList<Class<?>> findMappableEntityTypes() {
		return entityMetaDataProvider.findMappableEntityTypes();
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(Class<?> valueType) {
		return entityMetaDataProvider.getValueObjectConfig(valueType);
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(String xmlTypeName) {
		return entityMetaDataProvider.getValueObjectConfig(xmlTypeName);
	}

	@Override
	public List<Class<?>> getValueObjectTypesByEntityType(Class<?> entityType) {
		return entityMetaDataProvider.getValueObjectTypesByEntityType(entityType);
	}

	@Override
	public void toDotGraph(Writer writer) {
		entityMetaDataProvider.toDotGraph(writer);
	}
}
