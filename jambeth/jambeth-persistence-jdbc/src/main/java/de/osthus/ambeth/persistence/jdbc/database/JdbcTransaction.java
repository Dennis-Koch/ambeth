package de.osthus.ambeth.persistence.jdbc.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map.Entry;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.transaction.UserTransaction;

import de.osthus.ambeth.cache.ITransactionalRootCache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.database.DatabaseCallback;
import de.osthus.ambeth.database.IDatabaseProvider;
import de.osthus.ambeth.database.IDatabaseProviderRegistry;
import de.osthus.ambeth.database.IDatabaseSessionIdController;
import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.database.ITransactionInfo;
import de.osthus.ambeth.database.ITransactionListener;
import de.osthus.ambeth.database.ITransactionListenerProvider;
import de.osthus.ambeth.database.ResultingDatabaseCallback;
import de.osthus.ambeth.event.DatabaseAcquireEvent;
import de.osthus.ambeth.event.DatabaseCommitEvent;
import de.osthus.ambeth.event.DatabaseFailEvent;
import de.osthus.ambeth.event.DatabasePreCommitEvent;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.Forkable;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.ILightweightTransaction;
import de.osthus.ambeth.merge.ITransactionState;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.jdbc.IConnectionHolder;
import de.osthus.ambeth.persistence.jdbc.IConnectionHolderRegistry;
import de.osthus.ambeth.persistence.parallel.IModifyingDatabase;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.SensitiveThreadLocal;

public class JdbcTransaction implements ILightweightTransaction, ITransaction, ITransactionState, IThreadLocalCleanupBean
{
	public static class ThreadLocalItem implements ITransactionInfo
	{
		public Boolean ignoreReleaseDatabase;

		public Boolean alreadyOnStack;

		public Long sessionId;

		public Boolean beginInProgress;

		public Boolean isReadOnly;

		public Boolean etmActive;

		public LinkedHashMap<Object, IDatabase> databaseMap;

		public ArrayList<IBackgroundWorkerDelegate> preCommitRunnables;

		public boolean lazyMode;

		public long openTime;

		@Override
		public long getSessionId()
		{
			return sessionId != null ? sessionId.longValue() : 0;
		}

		@Override
		public boolean isReadOnly()
		{
			return isReadOnly != null ? isReadOnly.booleanValue() : false;
		}
	}

	@LogInstance
	private ILogger log;

	@Autowired
	protected IConnectionHolderRegistry connectionHolderRegistry;

	@Autowired
	protected IDatabaseProviderRegistry databaseProviderRegistry;

	@Autowired
	protected IDatabaseSessionIdController databaseSessionIdController;

	@Autowired(optional = true)
	protected IEventDispatcher eventDispatcher;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected ITransactionListenerProvider transactionListenerProvider;

	@Autowired(optional = true)
	protected ITransactionalRootCache transactionalRootCache;

	@Autowired(optional = true)
	protected UserTransaction userTransaction;

	@Forkable
	protected final ThreadLocal<ThreadLocalItem> tliTL = new SensitiveThreadLocal<ThreadLocalItem>();

	protected ThreadLocalItem getEnsureTLI()
	{
		ThreadLocalItem tli = tliTL.get();
		if (tli == null)
		{
			tli = new ThreadLocalItem();
			tliTL.set(tli);
		}
		return tli;
	}

	@Override
	public boolean isTransactionActive()
	{
		return isActive();
	}

	@Override
	public ITransactionInfo getTransactionInfo()
	{
		ThreadLocalItem tli = tliTL.get();
		if (tli != null && tli.sessionId != null)
		{
			return tli;
		}
		return null;
	}

	@Override
	public void begin(boolean readonly)
	{
		ThreadLocalItem tli = getEnsureTLI();
		if (Boolean.TRUE.equals(tli.alreadyOnStack))
		{
			return;
		}
		try
		{
			if (userTransaction != null)
			{
				tli.alreadyOnStack = Boolean.TRUE;
				try
				{
					userTransaction.begin();
				}
				finally
				{
					tli.alreadyOnStack = null;
				}
			}
			ILinkedMap<Object, IDatabaseProvider> persistenceUnitToDatabaseProviderMap = databaseProviderRegistry.getPersistenceUnitToDatabaseProviderMap();
			ILinkedMap<Object, IConnectionHolder> persistenceUnitToConnectionHolderMap = connectionHolderRegistry.getPersistenceUnitToConnectionHolderMap();

			LinkedHashMap<Object, IDatabase> persistenceUnitToDatabaseMap = new LinkedHashMap<Object, IDatabase>();

			long sessionId = databaseSessionIdController.acquireSessionId();
			tli.sessionId = new Long(sessionId);
			tli.isReadOnly = Boolean.valueOf(readonly);
			if (sessionId != -1 && eventDispatcher != null && !readonly)
			{
				// ReadOnly transactions will never invoke a DataChange. So there is no need to dispatch Database-Events to flush DataChanges
				eventDispatcher.dispatchEvent(new DatabaseAcquireEvent(sessionId));
			}
			tli.databaseMap = persistenceUnitToDatabaseMap;
			tli.openTime = System.currentTimeMillis();
			tli.beginInProgress = Boolean.TRUE;
			try
			{
				for (Entry<Object, IDatabaseProvider> entry : persistenceUnitToDatabaseProviderMap)
				{
					Object persistenceUnit = entry.getKey();
					IDatabaseProvider databaseProvider = entry.getValue();
					IDatabase database = databaseProvider.tryGetInstance();
					if (database != null)
					{
						if (database.getSessionId() != -1)
						{
							throw new IllegalStateException("Not all database providers have the same state regarding current database init");
						}
						database.setSessionId(sessionId);
					}
					else
					{
						database = databaseProvider.acquireInstance(readonly);
						database.setSessionId(sessionId);
						Connection connection = database.getAutowiredBeanInContext(Connection.class);
						if (connection == null)
						{
							throw new IllegalStateException(Connection.class.getName() + " expected in context of database handle");
						}
						IConnectionHolder connectionHolder = persistenceUnitToConnectionHolderMap.get(persistenceUnit);
						connectionHolder.setConnection(connection);
						if (transactionalRootCache != null && !readonly)
						{
							transactionalRootCache.acquireTransactionalRootCache();
						}
					}
					persistenceUnitToDatabaseMap.put(persistenceUnit, database);
				}
			}
			finally
			{
				tli.beginInProgress = null;
			}
			ITransactionListener[] transactionListeners = transactionListenerProvider.getTransactionListeners();
			for (ITransactionListener transactionListener : transactionListeners)
			{
				try
				{
					transactionListener.handlePostBegin(sessionId);
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
			if (eventDispatcher != null)
			{
				eventDispatcher.dispatchEvent(new TransactionBeginEvent(persistenceUnitToDatabaseMap));
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void commit()
	{
		ThreadLocalItem tli = getEnsureTLI();
		if (Boolean.TRUE.equals(tli.alreadyOnStack))
		{
			return;
		}
		Long sessionIdValue = tli.sessionId;
		if (sessionIdValue == null)
		{
			return;
		}
		long tillPreCommitTime = System.currentTimeMillis();
		boolean releaseSessionId = false;
		long sessionId = sessionIdValue.longValue();

		eventDispatcher.dispatchEvent(new DatabasePreCommitEvent(sessionId));
		ITransactionListener[] transactionListeners = transactionListenerProvider.getTransactionListeners();
		for (ITransactionListener transactionListener : transactionListeners)
		{
			try
			{
				transactionListener.handlePreCommit(sessionId);
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		ArrayList<IBackgroundWorkerDelegate> preCommitRunnables = tli.preCommitRunnables;
		while (preCommitRunnables != null && preCommitRunnables.size() > 0)
		{
			IBackgroundWorkerDelegate[] preCommitRunnablesArray = preCommitRunnables.toArray(IBackgroundWorkerDelegate.class);
			preCommitRunnables.clear();
			for (int a = 0, size = preCommitRunnablesArray.length; a < size; a++)
			{
				try
				{
					preCommitRunnablesArray[a].invoke();
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		}

		ILinkedMap<Object, IDatabaseProvider> persistenceUnitToDatabaseProviderMap = databaseProviderRegistry.getPersistenceUnitToDatabaseProviderMap();
		ILinkedMap<Object, IConnectionHolder> persistenceUnitToConnectionHolderMap = connectionHolderRegistry.getPersistenceUnitToConnectionHolderMap();
		try
		{
			long tillFlushTime = System.currentTimeMillis();
			for (Entry<Object, IDatabaseProvider> entry : persistenceUnitToDatabaseProviderMap)
			{
				IDatabaseProvider databaseProvider = entry.getValue();
				IDatabase database = databaseProvider.tryGetInstance();
				IModifyingDatabase modifyingDatabase = database.getAutowiredBeanInContext(IModifyingDatabase.class);
				if (modifyingDatabase.isModifyingAllowed())
				{
					database.flush();
				}
				else
				{
					database.revert();
				}
				if (transactionalRootCache != null)
				{
					transactionalRootCache.disposeTransactionalRootCache(true);
				}
			}
			if (userTransaction != null)
			{
				tli.alreadyOnStack = Boolean.TRUE;
				try
				{
					userTransaction.commit();
				}
				finally
				{
					tli.alreadyOnStack = null;
				}
			}
			LinkedHashMap<Object, IDatabase> databaseMap = tli.databaseMap;

			for (Entry<Object, IDatabaseProvider> entry : persistenceUnitToDatabaseProviderMap)
			{
				IDatabaseProvider databaseProvider = entry.getValue();
				IDatabase database = databaseProvider.tryGetInstance();
				database.setSessionId(-1);
			}
			long openTime = tli.openTime;
			if (eventDispatcher != null)
			{
				Boolean oldReadOnly = tli.isReadOnly;
				Boolean oldIgnoreReleaseDatabase = tli.ignoreReleaseDatabase;
				tli.openTime = 0;
				tli.databaseMap = null;
				tli.sessionId = null;
				tli.isReadOnly = null;
				tli.ignoreReleaseDatabase = Boolean.TRUE;
				try
				{
					eventDispatcher.dispatchEvent(new DatabaseCommitEvent(sessionId));
				}
				finally
				{
					tli.openTime = openTime;
					tli.ignoreReleaseDatabase = oldIgnoreReleaseDatabase;
					tli.sessionId = sessionIdValue;
					tli.databaseMap = databaseMap;
					tli.isReadOnly = oldReadOnly;
				}
			}
			if (!Boolean.TRUE.equals(tli.ignoreReleaseDatabase))
			{
				for (Entry<Object, IDatabaseProvider> entry : persistenceUnitToDatabaseProviderMap)
				{
					Object persistenceUnit = entry.getKey();
					IDatabaseProvider databaseProvider = entry.getValue();
					IDatabase database = databaseProvider.tryGetInstance();

					IConnectionHolder connectionHolder = persistenceUnitToConnectionHolderMap.get(persistenceUnit);
					if (connectionHolder != null)
					{
						connectionHolder.setConnection(null);
					}
					database.release(false);
				}
			}
			tli.openTime = 0;
			tli.sessionId = null;
			tli.databaseMap = null;
			tli.isReadOnly = null;
			releaseSessionId = true;
			if (log.isDebugEnabled())
			{
				long currTime = System.currentTimeMillis();
				long overall = currTime - openTime;
				long app = tillPreCommitTime - openTime;
				long preCommit = tillFlushTime - tillPreCommitTime;
				long flush = currTime - tillFlushTime;
				log.debug("Transaction commit (overall // app / preCommit / flush): " + overall + " // " + app + " / " + preCommit + " / " + flush + " ms");
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			if (releaseSessionId)
			{
				databaseSessionIdController.releaseSessionId(sessionId);
			}
		}
	}

	@Override
	public void rollback(boolean fatalError)
	{
		ThreadLocalItem tli = getEnsureTLI();
		if (Boolean.TRUE.equals(tli.alreadyOnStack))
		{
			return;
		}
		Long sessionIdValue = tli.sessionId;
		if (sessionIdValue == null)
		{
			return;
		}
		long sessionId = sessionIdValue.longValue();
		try
		{
			long openTime = tli.openTime;
			long preRollbackTime = System.currentTimeMillis();
			tli.openTime = 0;
			tli.sessionId = null;
			ILinkedMap<Object, IDatabase> databaseMap = tli.databaseMap;
			tli.databaseMap = null;
			Boolean readOnly = tli.isReadOnly;
			tli.isReadOnly = null;
			if (!Boolean.TRUE.equals(tli.ignoreReleaseDatabase))
			{
				ILinkedMap<Object, IConnectionHolder> persistenceUnitToConnectionHolderMap = connectionHolderRegistry.getPersistenceUnitToConnectionHolderMap();

				for (Entry<Object, IDatabase> entry : databaseMap)
				{
					Object persistenceUnit = entry.getKey();
					IDatabase database = entry.getValue();

					IConnectionHolder connectionHolder = persistenceUnitToConnectionHolderMap.get(persistenceUnit);

					if (connectionHolder != null)
					{
						connectionHolder.setConnection(null);
					}
					database.revert();
					if (transactionalRootCache != null)
					{
						transactionalRootCache.disposeTransactionalRootCache(false);
					}
					database.release(fatalError);
				}
			}
			else if (readOnly)
			{
				for (Entry<Object, IDatabase> entry : databaseMap)
				{
					IDatabase database = entry.getValue();

					database.revert();
					database.setSessionId(-1);
				}
			}
			if (userTransaction != null)
			{
				tli.alreadyOnStack = Boolean.TRUE;
				try
				{
					userTransaction.rollback();
				}
				finally
				{
					tli.alreadyOnStack = null;
				}
			}
			ITransactionListener[] transactionListeners = transactionListenerProvider.getTransactionListeners();
			for (ITransactionListener transactionListener : transactionListeners)
			{
				try
				{
					transactionListener.handlePostRollback(sessionId);
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
			long tillFlushTime = System.currentTimeMillis();
			if (eventDispatcher != null && !Boolean.TRUE.equals(readOnly))
			{
				eventDispatcher.dispatchEvent(new DatabaseFailEvent(sessionId));
			}
			if (log.isDebugEnabled())
			{
				long currTime = System.currentTimeMillis();
				long overall = currTime - openTime;
				long app = preRollbackTime - openTime;
				long preRollback = tillFlushTime - preRollbackTime;
				long revert = currTime - tillFlushTime;
				log.debug("Transaction rollback (overall // app / preRollback / revert): " + overall + " // " + app + " / " + preRollback + " / " + revert
						+ " ms");
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			databaseSessionIdController.releaseSessionId(sessionId);
		}
	}

	@Override
	public void processAndCommit(DatabaseCallback databaseCallback)
	{
		processAndCommit(databaseCallback, false, false);
	}

	@Override
	public void processAndCommit(DatabaseCallback databaseCallback, boolean expectOwnDatabaseSession, boolean readOnly)
	{
		ThreadLocalItem tli = getEnsureTLI();
		if (isActive())
		{
			if (expectOwnDatabaseSession)
			{
				throw new IllegalStateException("Transaction already active");
			}
			ILinkedMap<Object, IDatabase> databaseMap = Boolean.TRUE.equals(tli.beginInProgress) ? null : tli.databaseMap;
			try
			{
				databaseCallback.callback(databaseMap);
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			return;
		}
		boolean success = false;
		Exception recoverableException = null;
		try
		{
			begin(readOnly);
			ILinkedMap<Object, IDatabase> databaseMap = tli.databaseMap;
			databaseCallback.callback(databaseMap);
			if (readOnly)
			{
				rollback(false);
			}
			else
			{
				commit();
			}
			success = true;
		}
		catch (OptimisticLockException e)
		{
			recoverableException = e;
			throw e;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			if (!success)
			{
				rollback(recoverableException == null);
			}
		}
	}

	@Override
	public <R> R processAndCommit(ResultingDatabaseCallback<R> databaseCallback)
	{
		return processAndCommit(databaseCallback, false, false, false);
	}

	@Override
	public <R> R processAndCommit(ResultingDatabaseCallback<R> databaseCallback, boolean expectOwnDatabaseSession, boolean readOnly)
	{
		return processAndCommit(databaseCallback, expectOwnDatabaseSession, readOnly, false);
	}

	public <R> R processAndCommit(ResultingDatabaseCallback<R> databaseCallback, boolean expectOwnDatabaseSession, boolean readOnly, boolean lazyTransaction)
	{
		ThreadLocalItem tli = getEnsureTLI();
		if (isActive())
		{
			if (expectOwnDatabaseSession)
			{
				throw new IllegalStateException("Transaction already active");
			}
			ILinkedMap<Object, IDatabase> databaseMap = Boolean.TRUE.equals(tli.beginInProgress) ? null : tli.databaseMap;
			try
			{
				return databaseCallback.callback(databaseMap);
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		if (lazyTransaction)
		{
			boolean oldLazyMode = tli.lazyMode;
			if (oldLazyMode)
			{
				// nothing to do. any success or error will be handled already in the outer scope
				try
				{
					return databaseCallback.callback(null);
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
			tli.lazyMode = true;
			boolean success = false;
			Throwable recoverableException = null;
			try
			{
				R result = databaseCallback.callback(null);
				if (!isActive())
				{
					// during the callback no transaction has been opened & pending for close
					// so we have nothing to do in this case
					return result;
				}
				if (readOnly)
				{
					rollback(false);
				}
				else
				{
					commit();
				}
				success = true;
				return result;
			}
			catch (OptimisticLockException e)
			{
				recoverableException = e;
				throw e;
			}
			catch (PersistenceException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			catch (SQLException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			catch (Error e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			catch (Throwable e) // all other exceptions are assumed application based and therefore recoverable
			{
				recoverableException = e;
				throw RuntimeExceptionUtil.mask(e);
			}
			finally
			{
				tli.lazyMode = false;
				if (!success)
				{
					rollback(recoverableException == null);
				}
			}
		}
		if (tli.lazyMode)
		{
			// previous call to this JdbcTransaction with "lazy" flag. So we handle rollbacks/success at the "previous" outer level
			try
			{
				begin(false); // intentionally open the transaction "writable" in any case if we are in lazy mode
				ILinkedMap<Object, IDatabase> databaseMap = tli.databaseMap;
				return databaseCallback.callback(databaseMap);
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		boolean success = false;
		Exception recoverableException = null;
		try
		{
			begin(readOnly);
			ILinkedMap<Object, IDatabase> databaseMap = tli.databaseMap;
			R result = databaseCallback.callback(databaseMap);
			if (readOnly)
			{
				rollback(false);
			}
			else
			{
				commit();
			}
			success = true;
			return result;
		}
		catch (OptimisticLockException e)
		{
			recoverableException = e;
			throw e;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			if (!success)
			{
				rollback(recoverableException == null);
			}
		}
	}

	@Override
	public boolean isActive()
	{
		return getTransactionInfo() != null;
	}

	@Override
	public void cleanupThreadLocal()
	{
		if (getTransactionInfo() == null)
		{
			tliTL.remove();
			return;
		}
		throw new UnsupportedOperationException(
				"It is not supported to clean this ThreadLocal while you are in a transaction because this can lead to an inconsistent state");
	}

	@Override
	public Boolean isExternalTransactionManagerActive()
	{
		ThreadLocalItem tli = tliTL.get();
		if (tli == null)
		{
			return null;
		}
		return tli.etmActive;
	}

	@Override
	public void setExternalTransactionManagerActive(Boolean active)
	{
		ThreadLocalItem tli = null;
		if (active == null)
		{
			tli = tliTL.get();
			if (tli == null)
			{
				// Nothing to do in this case
				return;
			}
		}
		if (tli == null)
		{
			tli = getEnsureTLI();
		}
		tli.etmActive = active;
	}

	@Override
	public void runInTransaction(final IBackgroundWorkerDelegate runnable)
	{
		processAndCommit(new DatabaseCallback()
		{
			@Override
			public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Throwable
			{
				runnable.invoke();
			}
		});
	}

	@Override
	public <R> R runInTransaction(final IResultingBackgroundWorkerDelegate<R> runnable)
	{
		return processAndCommit(new ResultingDatabaseCallback<R>()
		{
			@Override
			public R callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Throwable
			{
				return runnable.invoke();
			}
		});
	}

	@Override
	public <R> R runInLazyTransaction(final IResultingBackgroundWorkerDelegate<R> runnable)
	{
		return processAndCommit(new ResultingDatabaseCallback<R>()
		{
			@Override
			public R callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Throwable
			{
				return runnable.invoke();
			}
		}, false, false, true);
	}

	@Override
	public void runOnTransactionPreCommit(IBackgroundWorkerDelegate runnable)
	{
		ThreadLocalItem tli = tliTL.get();
		if (tli == null || tli.sessionId == null)
		{
			throw new IllegalStateException("No transaction is currently active");
		}
		if (tli.preCommitRunnables == null)
		{
			tli.preCommitRunnables = new ArrayList<IBackgroundWorkerDelegate>();
		}
		tli.preCommitRunnables.add(runnable);
	}
}
