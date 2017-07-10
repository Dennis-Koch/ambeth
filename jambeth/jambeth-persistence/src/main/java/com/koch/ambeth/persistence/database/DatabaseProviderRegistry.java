package com.koch.ambeth.persistence.database;

/*-
 * #%L
 * jambeth-persistence
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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.extendable.MapExtendableContainer;
import com.koch.ambeth.persistence.api.database.IDatabaseProvider;
import com.koch.ambeth.util.collections.ILinkedMap;

public class DatabaseProviderRegistry
		implements IInitializingBean, IDatabaseProviderExtendable, IDatabaseProviderRegistry {
	protected MapExtendableContainer<Object, IDatabaseProvider> extensions = new MapExtendableContainer<>(
			"databaseProvider", "persistenceUnitId");

	@Override
	public void afterPropertiesSet() throws Throwable {
		// Intended blank
	}

	@Override
	public void registerDatabaseProvider(IDatabaseProvider databaseProvider,
			Object persistenceUnitId) {
		extensions.register(databaseProvider, persistenceUnitId);
	}

	@Override
	public void unregisterDatabaseProvider(IDatabaseProvider databaseProvider,
			Object persistenceUnitId) {
		extensions.unregister(databaseProvider, persistenceUnitId);
	}

	@Override
	public ILinkedMap<Object, IDatabaseProvider> getPersistenceUnitToDatabaseProviderMap() {
		return extensions.getExtensions();
	}
}
