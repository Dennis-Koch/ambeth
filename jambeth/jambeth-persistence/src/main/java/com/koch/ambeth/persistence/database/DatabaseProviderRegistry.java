package com.koch.ambeth.persistence.database;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.extendable.MapExtendableContainer;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.api.database.IDatabaseProvider;
import com.koch.ambeth.util.collections.ILinkedMap;

public class DatabaseProviderRegistry implements IInitializingBean, IDatabaseProviderExtendable, IDatabaseProviderRegistry
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected MapExtendableContainer<Object, IDatabaseProvider> extensions = new MapExtendableContainer<Object, IDatabaseProvider>("databaseProvider",
			"persistenceUnitId");

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
	}

	@Override
	public void registerDatabaseProvider(IDatabaseProvider databaseProvider, Object persistenceUnitId)
	{
		extensions.register(databaseProvider, persistenceUnitId);
	}

	@Override
	public void unregisterDatabaseProvider(IDatabaseProvider databaseProvider, Object persistenceUnitId)
	{
		extensions.unregister(databaseProvider, persistenceUnitId);
	}

	@Override
	public ILinkedMap<Object, IDatabaseProvider> getPersistenceUnitToDatabaseProviderMap()
	{
		return extensions.getExtensions();
	}
}
