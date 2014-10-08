package de.osthus.ambeth.persistence.jdbc.database;

import java.sql.Connection;
import java.util.Map.Entry;

import javax.persistence.OptimisticLockException;
import javax.transaction.UserTransaction;

import de.osthus.ambeth.cache.ITransactionalRootCache;
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
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.ITransactionState;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.jdbc.IConnectionHolder;
import de.osthus.ambeth.persistence.jdbc.IConnectionHolderRegistry;
import de.osthus.ambeth.persistence.parallel.IModifyingDatabase;
import de.osthus.ambeth.threading.SensitiveThreadLocal;
import de.osthus.ambeth.util.StringBuilderUtil;

public class JdbcTransaction implements ITransaction, ITransactionState, IThreadLocalCleanupBean
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
		ILinkedMap<Object, IDatabaseProvider> persistenceUnitToDatabaseProviderMap = databaseProviderRegistry.getPersistenceUnitToDatabaseProviderMap();
		ILinkedMap<Object, IConnectionHolder> persistenceUnitToConnectionHolderMap = connectionHolderRegistry.getPersistenceUnitToConnectionHolderMap();
		try
		{
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
			if (eventDispatcher != null)
			{
				Boolean oldReadOnly = tli.isReadOnly;
				Boolean oldIgnoreReleaseDatabase = tli.ignoreReleaseDatabase;
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
			tli.sessionId = null;
			tli.databaseMap = null;
			tli.isReadOnly = null;
			releaseSessionId = true;
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
			if (eventDispatcher != null && !Boolean.TRUE.equals(readOnly))
			{
				eventDispatcher.dispatchEvent(new DatabaseFailEvent(sessionId));
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
		long start = System.currentTimeMillis();
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
			long end = System.currentTimeMillis();
			if (log.isDebugEnabled())
			{
				log.debug(StringBuilderUtil.concat(objectCollector.getCurrent(), "Executed Transaction: ", (end - start), " ms"));
			}
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
		return processAndCommit(databaseCallback, false, false);
	}

	@Override
	public <R> R processAndCommit(ResultingDatabaseCallback<R> databaseCallback, boolean expectOwnDatabaseSession, boolean readOnly)
	{
		long start = System.currentTimeMillis();
		ThreadLocalItem tli = getEnsureTLI();
		if (isActive())
		{
			if (expectOwnDatabaseSession)
			{
				throw new IllegalStateException("Transaction already active");
			}
			// atabaseMap = databaseMapTL.get();
			// ILinkedMap<IDatabase, Object[]> oldContextProviderValues = IdentityLinkedMap.create(objectCollector.getCurrent());
			// try
			// {
			// IMapIterator<Object, IDatabase> iter = databaseMap.iterator();
			// while (iter.hasNext())
			// {
			// IDatabase database = iter.next();
			//
			// IContextProvider contextProvider = database.getContextProvider();
			// IUserHandle userHandle = securityScopeProvider.getUserHandle();
			// this.contextProvider.setCurrentUser(userHandle != null ? userHandle.getSid() : "anonymous");
			// this.contextProvider.setCurrentTime(Long.valueOf(System.currentTimeMillis()));
			// }
			// iter.dispose();
			// return databaseCallback.callback(databaseMap);
			// }
			// catch (Throwable e)
			// {
			// throw RuntimeExceptionUtil.mask(e);
			// }
			// finally
			// {
			// IMapIterator<IDatabase, Object> iter = oldContextProviderValues.iterator();
			// while (iter.hasNext())
			// {
			// IMapEntry<IDatabase, Object> entry = iter.nextEntry();
			// IContextProvider contextProvider = entry.getKey().getContextProvider();
			// Object[] oldValues = entry.getValue();
			// contextProvider.setCurrentTime(oldValues.)
			// }
			// oldContextProviderValues.dispose();
			// }
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
			long end = System.currentTimeMillis();
			if (log.isDebugEnabled())
			{
				log.debug(StringBuilderUtil.concat(objectCollector.getCurrent(), "Executed Transaction: ", (end - start), " ms"));
			}
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
}
