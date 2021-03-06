package com.koch.ambeth.persistence.jdbc;

/*-
 * #%L
 * jambeth-persistence-jdbc
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

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.NoopDatabasePool;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.IDatabaseDisposeHook;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.persistence.parallel.IModifyingDatabase;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IdentityWeakHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class DefaultDatabasePool extends NoopDatabasePool implements IDatabaseDisposeHook {
	@LogInstance
	private ILogger log;

	protected int maxUsedCount;

	protected int maxUnusedCount;

	protected int maxPendingCount;

	protected long maxPendingTryTime;

	protected volatile int pendingDatabases;

	protected boolean passivateDatabases;

	protected final Condition notEmptyCond = notFullCond;

	protected final IdentityWeakHashMap<IDatabase, Boolean> usedDatabases =
			new IdentityWeakHashMap<>();

	protected final ArrayList<IDatabase> unusedDatabases = new ArrayList<>();

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();

		ParamChecker.assertTrue(maxUsedCount > 0, "maxUsedCount");
		ParamChecker.assertTrue(maxUnusedCount > 0, "maxUnusedCount");
		ParamChecker.assertTrue(maxPendingCount > 0, "maxPendingCount");
		ParamChecker.assertTrue(maxPendingTryTime > 0, "pendingTryTimeSpan");
	}

	@Property(name = PersistenceConfigurationConstants.DatabasePoolMaxUsed, defaultValue = "2")
	public void setMaxUsedCount(int maxUsedCount) {
		this.maxUsedCount = maxUsedCount;
	}

	@Property(name = PersistenceConfigurationConstants.DatabasePoolMaxUnused, defaultValue = "2")
	public void setMaxUnusedCount(int maxUnusedCount) {
		this.maxUnusedCount = maxUnusedCount;
	}

	@Property(name = PersistenceConfigurationConstants.DatabasePoolMaxPending, defaultValue = "2")
	public void setMaxPendingCount(int maxPendingCount) {
		this.maxPendingCount = maxPendingCount;
	}

	@Property(name = PersistenceConfigurationConstants.DatabasePoolTryTime, defaultValue = "30000")
	public void setMaxPendingTryTime(long maxPendingTryTime) {
		this.maxPendingTryTime = maxPendingTryTime;
	}

	@Property(name = PersistenceConfigurationConstants.DatabasePoolPassivate, defaultValue = "false")
	public void setPassivateDatabases(boolean passivateDatabases) {
		this.passivateDatabases = passivateDatabases;
	}

	@Override
	public void shutdown() {
		super.shutdown();
		ReentrantLock writeLock = this.writeLock;
		List<IDatabase> unusedDatabases = this.unusedDatabases;
		while (true) {
			IDatabase database;
			writeLock.lock();
			try {
				if (unusedDatabases.isEmpty()) {
					notFullCond.signalAll();
					return;
				}
				database = unusedDatabases.remove(unusedDatabases.size() - 1);
			}
			finally {
				writeLock.unlock();
			}
			database.dispose();
		}
	}

	@Override
	protected IDatabase acquireDatabaseIntern(boolean tryOnly, boolean readOnly) {
		if (shuttingDown) {
			throw new RuntimeException("DatabasePool is shutting down");
		}
		long currentTime = System.currentTimeMillis();
		long waitTill = currentTime + maxPendingTryTime;
		IDatabase database;
		ArrayList<IDatabase> unusedDatabases = this.unusedDatabases;
		IdentityWeakHashMap<IDatabase, Boolean> usedDatabases = this.usedDatabases;
		Lock writeLock = this.writeLock;
		while (true) {
			if (shuttingDown) {
				throw new RuntimeException("DatabasePool is shutting down");
			}
			long now = System.currentTimeMillis();
			if (now >= waitTill) {
				throw new IllegalStateException("Waited " + (now - currentTime)
						+ "ms without successfully receiving a database instance");
			}
			writeLock.lock();
			try {
				if (!unusedDatabases.isEmpty()) {
					database = unusedDatabases.remove(unusedDatabases.size() - 1);
					usedDatabases.put(database, Boolean.TRUE);
					break;
				}
				else if (tryOnly) {
					return null;
				}
				else if (usedDatabases.size() + pendingDatabases < maxUsedCount
						&& pendingDatabases < maxPendingCount) {
					IDatabase newDatabase = createNewDatabase();

					if (newDatabase == null) {
						// Something wrong occured
						continue;
					}
					database = newDatabase;
					usedDatabases.put(database, Boolean.TRUE);
					break;
				}
				try {
					// Wait the maximum remaining time
					notEmptyCond.await(waitTill - now, TimeUnit.MILLISECONDS);
				}
				catch (InterruptedException e) {
					continue;
				}
			}
			finally {
				writeLock.unlock();
			}
		}
		if (passivateDatabases) {
			databaseFactory.activate(database);
		}
		if (!database.test()) {
			writeLock.lock();
			try {
				usedDatabases.remove(database);
			}
			finally {
				writeLock.unlock();
			}
			database.dispose();
			return acquireDatabaseIntern(tryOnly, readOnly);
		}
		database.acquired(readOnly);
		IModifyingDatabase modifyingDatabase =
				database.getAutowiredBeanInContext(IModifyingDatabase.class);
		modifyingDatabase.setModifyingDatabase(false);
		return database;
	}

	@Override
	protected IDatabase createNewDatabase() {
		pendingDatabases++;
		ReentrantLock writeLock = this.writeLock;
		writeLock.unlock();
		try {
			IDatabase database = super.createNewDatabase();
			if (database != null) {
				database.registerDisposeHook(this);
			}
			return database;
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			writeLock.lock();
			pendingDatabases--;
		}
	}

	@Override
	public void releaseDatabase(IDatabase database, boolean backToPool) {
		ParamChecker.assertParamNotNull(database, "database");
		if (database.isDisposed()) {
			// Nothing to do
			return;
		}
		ReentrantLock writeLock = this.writeLock;
		writeLock.lock();
		try {
			if (usedDatabases.remove(database) == null) {
				throw new RuntimeException(
						"Database " + database + " has not been acquired from this pool");
			}
			try {
				database.getAutowiredBeanInContext(IModifyingDatabase.class).setModifyingDatabase(false);
				database.setSessionId(-1);
				if (backToPool && !shuttingDown && unusedDatabases.size() < maxUnusedCount) {
					if (passivateDatabases) {
						databaseFactory.passivate(database);
					}
					unusedDatabases.add(database);
					database = null;
				}
			}
			finally {
				notEmptyCond.signal();
			}
		}
		finally {
			writeLock.unlock();
		}
		if (database == null) {
			return;
		}
		try {
			database.dispose();
		}
		catch (Throwable e) {
			// Intended blank
		}
		notifyCallbacksClosed(database);
	}

	@Override
	public void databaseDisposed(IDatabase disposedDatabase) {
		disposedDatabase.unregisterDisposeHook(this);
		ReentrantLock writeLock = this.writeLock;
		writeLock.lock();
		try {
			usedDatabases.remove(disposedDatabase);
			unusedDatabases.remove(disposedDatabase);
			notEmptyCond.signal();
		}
		finally {
			writeLock.unlock();
		}
		notifyCallbacksClosed(disposedDatabase);
	}
}
