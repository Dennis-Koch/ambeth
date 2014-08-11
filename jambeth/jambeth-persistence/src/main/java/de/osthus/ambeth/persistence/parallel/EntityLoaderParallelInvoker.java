package de.osthus.ambeth.persistence.parallel;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.database.IDatabaseProvider;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IDatabasePool;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.persistence.jdbc.IConnectionHolder;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;
import de.osthus.ambeth.util.IAlreadyLoadedCache;
import de.osthus.ambeth.util.IParamHolder;
import de.osthus.ambeth.util.ParamHolder;

public class EntityLoaderParallelInvoker implements IEntityLoaderParallelInvoker
{
	@Autowired
	protected IConnectionHolder connectionHolder;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected IDatabasePool databasePool;

	@Autowired
	protected IDatabaseProvider databaseProvider;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired(optional = true)
	protected ExecutorService executor;

	@Autowired
	protected IThreadLocalCleanupController threadLocalCleanupController;

	/**
	 * If the database is marked as 'modifying' no parallel execution is allowed. This is due to the fact that the execution of modifying SQL operations within
	 * a transaction may change the result of SELECT operations afterwards. Parallel connections in READ_COMMITTED state will read the "old" uncommitted data.
	 * To ensure that the usage of the parallel invocation feature will never change a result we deactivate the feature as a pessimistic behavior
	 * 
	 * @param database
	 * @return true if parallel execution should not be allowed due to pessimistic behavior
	 */
	protected boolean isModifyingSession(IDatabase database)
	{
		IModifyingDatabase modifyingDatabase = database.getAutowiredBeanInContext(IModifyingDatabase.class);
		return modifyingDatabase.isModifyingDatabase();
	}

	@Override
	public <V extends AbstractParallelItem> void invokeAndWait(List<V> items, IBackgroundWorkerParamDelegate<V> run)
	{
		if (items.size() == 0)
		{
			return;
		}
		IDatabase database = this.database.getCurrent();
		if (items.size() == 1 || isModifyingSession(database))
		{
			for (int a = items.size(); a-- > 0;)
			{
				V item = items.get(a);
				try
				{
					run.invoke(item);
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
				writePendingInitToShared(database, item.cascadeTypeToPendingInit, item.sharedCascadeTypeToPendingInit);
			}
			return;
		}
		ParamHolder<Throwable> exHolder = new ParamHolder<Throwable>();
		CountDownLatch latch = new CountDownLatch(items.size());
		ReentrantLock parallelLock = new ReentrantLock();
		ArrayList<IAlreadyLoadedCache> alCacheSnapshots = new ArrayList<IAlreadyLoadedCache>(items.size());
		for (int a = items.size(); a-- > 1;)
		{
			V ppi = items.get(a);

			invokeParallel(database, ppi, parallelLock, exHolder, latch, alCacheSnapshots, run, false);
		}
		invokeParallel(database, items.get(0), parallelLock, exHolder, latch, alCacheSnapshots, run, true);
		waitForParallelFinish(latch, exHolder);
		IAlreadyLoadedCache alCache = database.getContextProvider().getAlreadyLoadedCache();
		for (int a = alCacheSnapshots.size(); a-- > 0;)
		{
			IAlreadyLoadedCache parallelAlCacheSnapshot = alCacheSnapshots.get(a);
			parallelAlCacheSnapshot.copyTo(alCache);
		}
	}

	protected void waitForParallelFinish(CountDownLatch latch, IParamHolder<Throwable> exHolder)
	{
		while (latch.getCount() > 0)
		{
			try
			{
				latch.await(1000, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e)
			{
				// Intended blank
			}
			// A parallel exception will be thrown here
			if (exHolder.getValue() != null)
			{
				Throwable ex = exHolder.getValue();
				throw RuntimeExceptionUtil.mask(ex, "Exception occured while executing retrieval in parallel");
			}
		}
	}

	protected <V extends AbstractParallelItem> void invokeParallel(final IDatabase database, final V item, final Lock parallelLock,
			final IParamHolder<Throwable> exHolder, final CountDownLatch latch, final List<IAlreadyLoadedCache> alCacheSnapshots,
			final IBackgroundWorkerParamDelegate<V> run, boolean isLastItem)
	{
		// TODO final IDatabase parallelDatabase = executor != null && !isLastItem ? databasePool.tryAcquireDatabase(true) : null;
		final IDatabase parallelDatabase = executor != null && !isLastItem ? databasePool.tryAcquireDatabase(true) : null;

		if (parallelDatabase == null)
		{
			try
			{
				run.invoke(item);

				parallelLock.lock();
				try
				{
					// Must be synchronized in all cases because there might already be an async processes in action
					writePendingInitToShared(database, item.cascadeTypeToPendingInit, item.sharedCascadeTypeToPendingInit);
				}
				finally
				{
					parallelLock.unlock();
				}
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			finally
			{
				latch.countDown();
			}
			return;
		}
		executor.execute(new Runnable()
		{
			@Override
			public void run()
			{
				IConnectionHolder connectionHolder = EntityLoaderParallelInvoker.this.connectionHolder;
				ThreadLocal<IDatabase> databaseLocal = EntityLoaderParallelInvoker.this.databaseProvider.getDatabaseLocal();
				try
				{
					databaseLocal.set(parallelDatabase);
					connectionHolder.setConnection(parallelDatabase.getAutowiredBeanInContext(Connection.class));

					run.invoke(item);

					parallelLock.lock();
					try
					{
						writePendingInitToShared(parallelDatabase, item.cascadeTypeToPendingInit, item.sharedCascadeTypeToPendingInit);

						// Copy forked AlCache information back to AlCache
						IAlreadyLoadedCache alCacheSnapshot = parallelDatabase.getContextProvider().getAlreadyLoadedCache().snapshot();
						alCacheSnapshots.add(alCacheSnapshot);
					}
					finally
					{
						parallelLock.unlock();
					}
				}
				catch (Throwable e)
				{
					parallelLock.lock();
					try
					{
						if (exHolder.getValue() == null)
						{
							exHolder.setValue(e);
						}
					}
					finally
					{
						parallelLock.unlock();
					}
				}
				finally
				{
					databaseLocal.remove();
					connectionHolder.setConnection(null);
					threadLocalCleanupController.cleanupThreadLocal();
					parallelDatabase.release(false);
					latch.countDown();
				}
			}
		});
	}

	protected void writePendingInitToShared(IDatabase database, LinkedHashMap<Class<?>, Collection<Object>[]> cascadeTypeToPendingInit,
			LinkedHashMap<Class<?>, Collection<Object>[]> sharedCascadeTypeToPendingInit)
	{
		for (Entry<Class<?>, Collection<Object>[]> entry : cascadeTypeToPendingInit)
		{
			Class<?> type = entry.getKey();

			if (!entityMetaDataProvider.getMetaData(type).isLocalEntity())
			{
				continue;
			}
			ITable table = database.getTableByType(type);

			Collection<Object>[] pendingInits = entry.getValue();
			for (int a = pendingInits.length; a-- > 0;)
			{
				Collection<Object> pendingInit = pendingInits[a];
				if (pendingInit == null)
				{
					continue;
				}
				Collection<Object> sharedPendingInit = getEnsurePendingInit(table, sharedCascadeTypeToPendingInit, (byte) (a - 1));
				sharedPendingInit.addAll(pendingInit);
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected Collection<Object> getEnsurePendingInit(Class<?> type, int alternateIdCount, Map<Class<?>, Collection<Object>[]> typeToPendingInit,
			byte idNameIndex)
	{
		Collection<Object>[] pendingInits = typeToPendingInit.get(type);
		if (pendingInits == null)
		{
			pendingInits = new Collection[alternateIdCount + 1];
			typeToPendingInit.put(type, pendingInits);
		}
		Collection<Object> pendingInit = pendingInits[idNameIndex + 1];
		if (pendingInit == null)
		{
			pendingInit = new HashSet<Object>();
			pendingInits[idNameIndex + 1] = pendingInit;
		}
		return pendingInit;
	}

	protected Collection<Object> getEnsurePendingInit(ITable table, Map<Class<?>, Collection<Object>[]> typeToPendingInit, byte idNameIndex)
	{
		return getEnsurePendingInit(table.getEntityType(), table.getAlternateIdCount(), typeToPendingInit, idNameIndex);
	}
}
