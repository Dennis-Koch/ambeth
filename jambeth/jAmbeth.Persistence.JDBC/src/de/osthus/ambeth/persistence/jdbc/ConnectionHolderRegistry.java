package de.osthus.ambeth.persistence.jdbc;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.extendable.MapExtendableContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class ConnectionHolderRegistry implements IInitializingBean, IConnectionHolderExtendable, IConnectionHolderRegistry
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected MapExtendableContainer<Object, IConnectionHolder> extensions = new MapExtendableContainer<Object, IConnectionHolder>("connectionHolder",
			"persistenceUnitId");

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
	}

	@Override
	public ILinkedMap<Object, IConnectionHolder> getPersistenceUnitToConnectionHolderMap()
	{
		return extensions.getExtensions();
	}

	@Override
	public void registerConnectionHolder(IConnectionHolder connectionHolder, Object persistenceUnitId)
	{
		extensions.register(connectionHolder, persistenceUnitId);
	}

	@Override
	public void unregisterConnectionHolder(IConnectionHolder connectionHolder, Object persistenceUnitId)
	{
		extensions.unregister(connectionHolder, persistenceUnitId);
	}
}
