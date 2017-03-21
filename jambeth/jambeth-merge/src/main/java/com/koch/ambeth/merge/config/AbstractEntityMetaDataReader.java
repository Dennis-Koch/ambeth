package com.koch.ambeth.merge.config;

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

import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IEntityMetaDataExtendable;
import com.koch.ambeth.merge.model.EntityMetaData;
import com.koch.ambeth.merge.orm.IEntityConfig;
import com.koch.ambeth.merge.orm.IOrmConfigGroup;
import com.koch.ambeth.merge.orm.IOrmConfigGroupProvider;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.collections.LinkedHashSet;

public abstract class AbstractEntityMetaDataReader implements IDisposableBean {
	@LogInstance
	private ILogger log;
	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;
	@Autowired
	protected IEntityMetaDataExtendable entityMetaDataExtendable;
	@Autowired
	protected IEventDispatcher eventDispatcher;
	@Autowired
	protected IEntityMetaDataReader entityMetaDataReader;
	@Autowired
	protected IOrmConfigGroupProvider ormConfigGroupProvider;
	protected final LinkedHashSet<IEntityMetaData> managedEntityMetaData =
			new LinkedHashSet<>();

	@Override
	public void destroy() {
		for (IEntityMetaData entityMetaData : managedEntityMetaData) {
			entityMetaDataExtendable.unregisterEntityMetaData(entityMetaData);
		}
	}

	protected void readConfig(IOrmConfigGroup ormConfigGroup) {
		LinkedHashSet<IEntityConfig> entities = new LinkedHashSet<>();
		entities.addAll(ormConfigGroup.getLocalEntityConfigs());
		entities.addAll(ormConfigGroup.getExternalEntityConfigs());

		for (IEntityConfig entityConfig : entities) {
			Class<?> entityType = entityConfig.getEntityType();
			if (entityMetaDataProvider.getMetaData(entityType, true) != null) {
				continue;
			}
			Class<?> realType = entityConfig.getRealType();

			EntityMetaData metaData = new EntityMetaData();
			metaData.setEntityType(entityType);
			metaData.setRealType(realType);
			metaData.setLocalEntity(entityConfig.isLocal());

			entityMetaDataReader.addMembers(metaData, entityConfig);

			managedEntityMetaData.add(metaData);
			synchronized (entityMetaDataExtendable) {
				entityMetaDataExtendable.registerEntityMetaData(metaData);
			}
		}
	}

}
