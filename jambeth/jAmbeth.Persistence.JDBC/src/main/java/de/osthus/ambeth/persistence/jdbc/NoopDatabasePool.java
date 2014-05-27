package de.osthus.ambeth.persistence.jdbc;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.IDatabaseLifecycleCallback;
import de.osthus.ambeth.IDatabasePool;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.database.IDatabaseFactory;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.callback.IDatabaseLifecycleCallbackRegistry;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.persistence.parallel.IModifyingDatabase;
import de.osthus.ambeth.util.ParamChecker;

public class NoopDatabasePool implements IDatabasePool, IInitializingBean
{
	@LogInstance
	private ILogger log;

	protected int pendingTryCount; // 0 -> no limit, keep trying

	protected long pendingTryTimeSpan;

	protected IDatabaseFactory databaseFactory;

	protected volatile boolean shuttingDown;

	protected IDatabaseLifecycleCallbackRegistry databaseLifecycleCallbackRegistry;

	protected final ReentrantLock writeLock = new ReentrantLock();

	protected final Condition notFullCond = writeLock.newCondition();

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(databaseFactory, "databaseFactory");
		ParamChecker.assertNotNull(databaseLifecycleCallbackRegistry, "databaseLifecycleCallbackRegistry");

		ParamChecker.assertTrue(pendingTryCount >= 0, "pendingTryCount");
		ParamChecker.assertTrue(pendingTryTimeSpan >= 0, "pendingTryTimeSpan");
	}

	public void setDatabaseFactory(IDatabaseFactory databaseFactory)
	{
		this.databaseFactory = databaseFactory;
	}

	public void setDatabaseLifecycleCallbackRegistry(IDatabaseLifecycleCallbackRegistry databaseLifecycleCallbackRegistry)
	{
		this.databaseLifecycleCallbackRegistry = databaseLifecycleCallbackRegistry;
	}

	@Property(name = PersistenceConfigurationConstants.DatabasePoolTryCount, defaultValue = "1")
	public void setPendingTryCount(int pendingTryCount)
	{
		this.pendingTryCount = pendingTryCount;
	}

	@Property(name = PersistenceConfigurationConstants.DatabasePoolTryTimeSpan, defaultValue = "30000")
	public void setPendingTryTimeSpan(long pendingTryTimeSpan)
	{
		this.pendingTryTimeSpan = pendingTryTimeSpan;
	}

	@Override
	public void shutdown()
	{
		shuttingDown = true;
	}

	@Override
	public IDatabase acquireDatabase()
	{
		return acquireDatabaseIntern(false, false);
	}

	@Override
	public IDatabase acquireDatabase(boolean readonlyMode)
	{
		return acquireDatabaseIntern(false, readonlyMode);
	}

	@Override
	public IDatabase tryAcquireDatabase()
	{
		return acquireDatabaseIntern(true, false);
	}

	@Override
	public IDatabase tryAcquireDatabase(boolean readonlyMode)
	{
		return acquireDatabaseIntern(true, readonlyMode);
	}

	protected IDatabase acquireDatabaseIntern(boolean tryOnly, boolean readOnly)
	{
		int currentTryCount = 0;
		long currentTime = System.currentTimeMillis();
		boolean keepTrying = !tryOnly;
		IDatabase database = null;

		while (keepTrying)
		{
			if (shuttingDown)
			{
				throw new RuntimeException("DatabasePool is shutting down");
			}

			if (pendingTryCount > 0 && currentTryCount > pendingTryCount)
			{
				throw new IllegalStateException("Tried " + pendingTryCount + " times waiting for " + (System.currentTimeMillis() - currentTime)
						+ "ms without successfully receiving a database instance");
			}

			database = createNewDatabase();
			currentTryCount++;
			if (database != null)
			{
				break;
			}
			if (keepTrying)
			{
				ReentrantLock writeLock = this.writeLock;
				writeLock.lock();
				try
				{
					try
					{
						notFullCond.await(pendingTryTimeSpan, TimeUnit.MILLISECONDS);
					}
					catch (InterruptedException e)
					{
						continue;
					}
				}
				finally
				{
					writeLock.unlock();
				}
			}
		}
		if (database != null)
		{
			database.acquired(readOnly);
		}
		return database;
	}

	protected IDatabase createNewDatabase()
	{
		try
		{
			IDatabase database = databaseFactory.createDatabaseInstance(this);
			notifyCallbacksCreated(database);
			return database;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void notifyCallbacksCreated(IDatabase database)
	{
		IDatabaseLifecycleCallback[] databaseLifecycleCallbacks = databaseLifecycleCallbackRegistry.getDatabaseLifecycleCallbacks();
		for (IDatabaseLifecycleCallback callback : databaseLifecycleCallbacks)
		{
			try
			{
				callback.databaseConnected(database);
			}
			catch (Throwable e)
			{
				log.error(e);
			}
		}
	}

	protected void notifyCallbacksClosed(IDatabase database)
	{
		IDatabaseLifecycleCallback[] databaseLifecycleCallbacks = databaseLifecycleCallbackRegistry.getDatabaseLifecycleCallbacks();
		for (IDatabaseLifecycleCallback callback : databaseLifecycleCallbacks)
		{
			try
			{
				callback.databaseClosed(database);
			}
			catch (Throwable e)
			{
				log.error(e);
			}
		}
	}

	@Override
	public void releaseDatabase(IDatabase database)
	{
		releaseDatabase(database, false);
	}

	@Override
	public void releaseDatabase(IDatabase database, boolean backToPool)
	{
		ParamChecker.assertParamNotNull(database, "database");
		database.getAutowiredBeanInContext(IModifyingDatabase.class).setModifyingDatabase(false);
		try
		{
			database.setSessionId(-1);
			database.dispose();
			notifyCallbacksClosed(database);
		}
		catch (Throwable e)
		{
			// Intended blank
		}
	}
}
