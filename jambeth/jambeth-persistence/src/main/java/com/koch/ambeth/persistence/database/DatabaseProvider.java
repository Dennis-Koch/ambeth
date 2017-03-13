package com.koch.ambeth.persistence.database;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.IDatabasePool;
import com.koch.ambeth.persistence.api.database.IDatabaseProvider;
import com.koch.ambeth.util.proxy.ITargetProvider;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;

public class DatabaseProvider implements ITargetProvider, IDatabaseProvider, IThreadLocalCleanupBean
{
	@Autowired
	protected IDatabasePool databasePool;

	@Property(defaultValue = "PERSISTENT")
	protected DatabaseType databaseType;

	@Forkable
	protected final SensitiveThreadLocal<IDatabase> databaseTL = new SensitiveThreadLocal<IDatabase>();

	@Override
	public void cleanupThreadLocal()
	{
		databaseTL.remove();
	}

	@Override
	public IDatabase tryGetInstance()
	{
		return databaseTL.get();
	}

	@Override
	public ThreadLocal<IDatabase> getDatabaseLocal()
	{
		return databaseTL;
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
		database = databasePool.acquireDatabase(readonly);
		databaseTL.set(database);
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
