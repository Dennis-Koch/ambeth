package de.osthus.ambeth.database;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.Forkable;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IDatabasePool;
import de.osthus.ambeth.proxy.ITargetProvider;
import de.osthus.ambeth.threading.SensitiveThreadLocal;

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
