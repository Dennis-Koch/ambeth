package de.osthus.ambeth.database;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.extendable.MapExtendableContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

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
