package com.koch.ambeth.persistence.jdbc;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.extendable.MapExtendableContainer;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.jdbc.IConnectionHolder;
import com.koch.ambeth.util.collections.ILinkedMap;

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
