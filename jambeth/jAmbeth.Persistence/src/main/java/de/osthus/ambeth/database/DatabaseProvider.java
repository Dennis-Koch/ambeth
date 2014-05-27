package de.osthus.ambeth.database;

import de.osthus.ambeth.IDatabasePool;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.proxy.ITargetProvider;
import de.osthus.ambeth.threading.SensitiveThreadLocal;
import de.osthus.ambeth.util.ParamChecker;

public class DatabaseProvider extends SensitiveThreadLocal<IDatabase> implements ITargetProvider, IInitializingBean, IDatabaseProvider
{
	protected IDatabasePool databasePool;

	protected DatabaseType databaseType = DatabaseType.PERSISTENT;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(databasePool, "DatabasePool");
		ParamChecker.assertNotNull(databaseType, "DatabaseType");
	}

	public void setDatabasePool(IDatabasePool databasePool)
	{
		this.databasePool = databasePool;
	}

	public void setDatabaseType(DatabaseType databaseType)
	{
		this.databaseType = databaseType;
	}

	@Override
	public IDatabase tryGetInstance()
	{
		return get();
	}

	@Override
	public ThreadLocal<IDatabase> getDatabaseLocal()
	{
		return this;
	}

	@Override
	public IDatabase acquireInstance()
	{
		return acquireInstance(false);
	}

	@Override
	public IDatabase acquireInstance(boolean readonly)
	{
		IDatabase database = tryGetInstance();
		if (database != null)
		{
			throw new RuntimeException("Instance already acquired. Maybe you must not acquire instances at your current application scope?");
		}
		database = this.databasePool.acquireDatabase(readonly);
		set(database);
		return database;
	}

	public IDatabase getInstance()
	{
		IDatabase database = tryGetInstance();
		if (database == null)
		{
			throw new RuntimeException("No instance acquired, yet. It should have been done at this point!"
					+ " If this exception happens within a service request from a client your service implementing method"
					+ " might not be specified as virtual. A service method must be to allow dynamic proxying" + " for database session operations");
		}
		return database;
	}

	@Override
	public Object getTarget()
	{
		return getInstance();
	}
}
