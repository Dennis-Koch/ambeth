package de.osthus.ambeth.merge;

import java.util.Map.Entry;

import de.osthus.ambeth.database.IDatabaseProvider;
import de.osthus.ambeth.database.IDatabaseProviderRegistry;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ParamChecker;

public class TransactionState implements IInitializingBean, ITransactionState
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IDatabaseProviderRegistry databaseProviderRegistry;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(databaseProviderRegistry, "DatabaseProviderRegistry");
	}

	public void setDatabaseProviderRegistry(IDatabaseProviderRegistry databaseProviderRegistry)
	{
		this.databaseProviderRegistry = databaseProviderRegistry;
	}

	@Override
	public boolean isTransactionActive()
	{
		for (Entry<Object, IDatabaseProvider> entry : databaseProviderRegistry.getPersistenceUnitToDatabaseProviderMap())
		{
			IDatabaseProvider databaseProvider = entry.getValue();
			if (databaseProvider.tryGetInstance() != null)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public Boolean isExternalTransactionManagerActive()
	{
		return null;
	}

	@Override
	public void setExternalTransactionManagerActive(Boolean active)
	{
		if (active != null)
		{
			throw new UnsupportedOperationException();
		}
	}
}
