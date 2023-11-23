package com.koch.ambeth.persistence.jdbc.database;

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

import com.koch.ambeth.cache.ITransactionalRootCacheManager;
import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.ILightweightTransaction;
import com.koch.ambeth.merge.ITransactionState;
import com.koch.ambeth.persistence.IConnectionHolder;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.database.DatabaseCallback;
import com.koch.ambeth.persistence.api.database.IDatabaseProvider;
import com.koch.ambeth.persistence.api.database.ITransaction;
import com.koch.ambeth.persistence.api.database.ITransactionInfo;
import com.koch.ambeth.persistence.api.database.ITransactionListener;
import com.koch.ambeth.persistence.api.database.ITransactionListenerProvider;
import com.koch.ambeth.persistence.api.database.ResultingDatabaseCallback;
import com.koch.ambeth.persistence.database.IDatabaseProviderRegistry;
import com.koch.ambeth.persistence.database.IDatabaseSessionIdController;
import com.koch.ambeth.persistence.event.DatabaseAcquireEvent;
import com.koch.ambeth.persistence.event.DatabaseCommitEvent;
import com.koch.ambeth.persistence.event.DatabaseFailEvent;
import com.koch.ambeth.persistence.event.DatabasePreCommitEvent;
import com.koch.ambeth.persistence.jdbc.IConnectionHolderRegistry;
import com.koch.ambeth.persistence.parallel.IModifyingDatabase;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.function.CheckedRunnable;
import com.koch.ambeth.util.function.CheckedSupplier;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.UserTransaction;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map.Entry;

public class JdbcTransaction implements ILightweightTransaction, ITransaction, ITransactionState, IThreadLocalCleanupBean {
    @Forkable
    protected final ThreadLocal<ThreadLocalItem> tliTL = new SensitiveThreadLocal<>();
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
    protected ITransactionalRootCacheManager transactionalRootCache;
    @Autowired(optional = true)
    protected UserTransaction userTransaction;
    @LogInstance
    private ILogger log;

    protected ThreadLocalItem getEnsureTLI() {
        ThreadLocalItem tli = tliTL.get();
        if (tli == null) {
            tli = new ThreadLocalItem();
            tliTL.set(tli);
        }
        return tli;
    }

    @Override
    public boolean isTransactionActive() {
        return isActive();
    }

    @Override
    public ITransactionInfo getTransactionInfo() {
        ThreadLocalItem tli = tliTL.get();
        if (tli != null && tli.sessionId != null) {
            return tli;
        }
        return null;
    }

    @Override
    public void begin(boolean readonly) {
        ThreadLocalItem tli = getEnsureTLI();
        if (Boolean.TRUE.equals(tli.alreadyOnStack)) {
            return;
        }
        try {
            if (userTransaction != null) {
                tli.alreadyOnStack = Boolean.TRUE;
                try {
                    userTransaction.begin();
                } finally {
                    tli.alreadyOnStack = null;
                }
            }
            ILinkedMap<Object, IDatabaseProvider> persistenceUnitToDatabaseProviderMap = databaseProviderRegistry.getPersistenceUnitToDatabaseProviderMap();
            ILinkedMap<Object, IConnectionHolder> persistenceUnitToConnectionHolderMap = connectionHolderRegistry.getPersistenceUnitToConnectionHolderMap();

            LinkedHashMap<Object, IDatabase> persistenceUnitToDatabaseMap = new LinkedHashMap<>();

            long sessionId = databaseSessionIdController.acquireSessionId();
            tli.sessionId = new Long(sessionId);
            tli.isReadOnly = Boolean.valueOf(readonly);
            if (sessionId != -1 && eventDispatcher != null && !readonly) {
                // ReadOnly transactions will never invoke a DataChange. So there is no need to dispatch
                // Database-Events to flush DataChanges
                eventDispatcher.dispatchEvent(new DatabaseAcquireEvent(sessionId));
            }
            tli.databaseMap = persistenceUnitToDatabaseMap;
            tli.openTime = System.currentTimeMillis();
            tli.beginInProgress = Boolean.TRUE;
            try {
                for (Entry<Object, IDatabaseProvider> entry : persistenceUnitToDatabaseProviderMap) {
                    Object persistenceUnit = entry.getKey();
                    IDatabaseProvider databaseProvider = entry.getValue();
                    IDatabase database = databaseProvider.tryGetInstance();
                    if (database != null) {
                        if (database.getSessionId() != -1) {
                            throw new IllegalStateException("Not all database providers have the same state regarding current database init");
                        }
                        database.setSessionId(sessionId);
                    } else {
                        database = databaseProvider.acquireInstance(readonly);
                        database.setSessionId(sessionId);
                        Connection connection = database.getAutowiredBeanInContext(Connection.class);
                        if (connection == null) {
                            throw new IllegalStateException(Connection.class.getName() + " expected in context of database handle");
                        }
                        IConnectionHolder connectionHolder = persistenceUnitToConnectionHolderMap.get(persistenceUnit);
                        connectionHolder.setConnection(connection);
                        if (transactionalRootCache != null && !readonly) {
                            transactionalRootCache.acquireTransactionalRootCache();
                        }
                    }
                    persistenceUnitToDatabaseMap.put(persistenceUnit, database);
                }
            } finally {
                tli.beginInProgress = null;
            }
            ITransactionListener[] transactionListeners = transactionListenerProvider.getTransactionListeners();
            for (ITransactionListener transactionListener : transactionListeners) {
                try {
                    transactionListener.handlePostBegin(sessionId);
                } catch (Exception e) {
                    throw RuntimeExceptionUtil.mask(e);
                }
            }
            if (eventDispatcher != null) {
                eventDispatcher.dispatchEvent(new TransactionBeginEvent(persistenceUnitToDatabaseMap));
            }
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    protected void notifyRunnables(ArrayList<CheckedRunnable> runnables) {
        while (runnables != null && !runnables.isEmpty()) {
            CheckedRunnable[] preCommitRunnablesArray = runnables.toArray(CheckedRunnable.class);
            runnables.clear();
            for (int a = preCommitRunnablesArray.length; a-- > 0; ) {
                CheckedRunnable.invoke(preCommitRunnablesArray[a]);
            }
        }
    }

    @Override
    public void commit() {
        ThreadLocalItem tli = getEnsureTLI();
        if (Boolean.TRUE.equals(tli.alreadyOnStack)) {
            return;
        }
        Long sessionIdValue = tli.sessionId;
        if (sessionIdValue == null) {
            return;
        }
        long tillPreCommitTime = System.currentTimeMillis();
        boolean releaseSessionId = false;
        long sessionId = sessionIdValue.longValue();

        eventDispatcher.dispatchEvent(new DatabasePreCommitEvent(sessionId));
        ITransactionListener[] transactionListeners = transactionListenerProvider.getTransactionListeners();
        for (ITransactionListener transactionListener : transactionListeners) {
            try {
                transactionListener.handlePreCommit(sessionId);
            } catch (Exception e) {
                throw RuntimeExceptionUtil.mask(e);
            }
        }
        notifyRunnables(tli.preCommitRunnables);
        ILinkedMap<Object, IDatabaseProvider> persistenceUnitToDatabaseProviderMap = databaseProviderRegistry.getPersistenceUnitToDatabaseProviderMap();
        ILinkedMap<Object, IConnectionHolder> persistenceUnitToConnectionHolderMap = connectionHolderRegistry.getPersistenceUnitToConnectionHolderMap();
        try {
            long tillFlushTime = System.currentTimeMillis();
            for (Entry<Object, IDatabaseProvider> entry : persistenceUnitToDatabaseProviderMap) {
                IDatabaseProvider databaseProvider = entry.getValue();
                IDatabase database = databaseProvider.tryGetInstance();
                IModifyingDatabase modifyingDatabase = database.getAutowiredBeanInContext(IModifyingDatabase.class);
                if (modifyingDatabase.isModifyingAllowed()) {
                    database.flush();
                } else {
                    database.revert();
                }
            }
            if (transactionalRootCache != null) {
                transactionalRootCache.disposeTransactionalRootCache(true);
            }
            if (userTransaction != null) {
                tli.alreadyOnStack = Boolean.TRUE;
                try {
                    userTransaction.commit();
                } finally {
                    tli.alreadyOnStack = null;
                }
            }
            LinkedHashMap<Object, IDatabase> databaseMap = tli.databaseMap;

            for (Entry<Object, IDatabaseProvider> entry : persistenceUnitToDatabaseProviderMap) {
                IDatabaseProvider databaseProvider = entry.getValue();
                IDatabase database = databaseProvider.tryGetInstance();
                database.setSessionId(-1);
            }
            tli.lazyMode = false;
            long openTime = tli.openTime;
            {
                Boolean oldReadOnly = tli.isReadOnly;
                Boolean oldIgnoreReleaseDatabase = tli.ignoreReleaseDatabase;
                tli.openTime = 0;
                tli.databaseMap = null;
                tli.sessionId = null;
                tli.isReadOnly = null;
                tli.ignoreReleaseDatabase = Boolean.TRUE;
                try {
                    notifyRunnables(tli.postCommitRunnables);
                    if (eventDispatcher != null) {
                        eventDispatcher.dispatchEvent(new DatabaseCommitEvent(sessionId));
                    }
                } finally {
                    tli.openTime = openTime;
                    tli.ignoreReleaseDatabase = oldIgnoreReleaseDatabase;
                    tli.sessionId = sessionIdValue;
                    tli.databaseMap = databaseMap;
                    tli.isReadOnly = oldReadOnly;
                }
            }
            if (!Boolean.TRUE.equals(tli.ignoreReleaseDatabase)) {
                for (Entry<Object, IDatabaseProvider> entry : persistenceUnitToDatabaseProviderMap) {
                    Object persistenceUnit = entry.getKey();
                    IDatabaseProvider databaseProvider = entry.getValue();
                    IDatabase database = databaseProvider.tryGetInstance();

                    IConnectionHolder connectionHolder = persistenceUnitToConnectionHolderMap.get(persistenceUnit);
                    if (connectionHolder != null) {
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
            if (log.isDebugEnabled()) {
                long currTime = System.currentTimeMillis();
                long overall = currTime - openTime;
                long app = tillPreCommitTime - openTime;
                long preCommit = tillFlushTime - tillPreCommitTime;
                long flush = currTime - tillFlushTime;
                log.debug("Transaction commit (overall // app / preCommit / flush): " + overall + " // " + app + " / " + preCommit + " / " + flush + " ms");
            }
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        } finally {
            if (releaseSessionId) {
                databaseSessionIdController.releaseSessionId(sessionId);
            }
        }
        for (ITransactionListener transactionListener : transactionListeners) {
            try {
                transactionListener.handlePostCommit(sessionId);
            } catch (Exception e) {
                throw RuntimeExceptionUtil.mask(e);
            }
        }
    }

    @Override
    public void rollback(boolean fatalError) {
        ThreadLocalItem tli = getEnsureTLI();
        if (Boolean.TRUE.equals(tli.alreadyOnStack)) {
            return;
        }
        Long sessionIdValue = tli.sessionId;
        if (sessionIdValue == null) {
            return;
        }
        long sessionId = sessionIdValue.longValue();
        try {
            long openTime = tli.openTime;
            long preRollbackTime = System.currentTimeMillis();
            tli.openTime = 0;
            tli.sessionId = null;
            ILinkedMap<Object, IDatabase> databaseMap = tli.databaseMap;
            tli.databaseMap = null;
            Boolean readOnly = tli.isReadOnly;
            tli.isReadOnly = null;
            tli.lazyMode = false;
            if (!Boolean.TRUE.equals(tli.ignoreReleaseDatabase)) {
                ILinkedMap<Object, IConnectionHolder> persistenceUnitToConnectionHolderMap = connectionHolderRegistry.getPersistenceUnitToConnectionHolderMap();

                for (Entry<Object, IDatabase> entry : databaseMap) {
                    Object persistenceUnit = entry.getKey();
                    IDatabase database = entry.getValue();

                    IConnectionHolder connectionHolder = persistenceUnitToConnectionHolderMap.get(persistenceUnit);

                    if (connectionHolder != null) {
                        connectionHolder.setConnection(null);
                    }
                    database.revert();
                    database.release(fatalError);
                }
                if (transactionalRootCache != null) {
                    transactionalRootCache.disposeTransactionalRootCache(false);
                }
            } else if (readOnly) {
                for (Entry<Object, IDatabase> entry : databaseMap) {
                    IDatabase database = entry.getValue();

                    database.revert();
                    database.setSessionId(-1);
                }
            }
            if (userTransaction != null) {
                tli.alreadyOnStack = Boolean.TRUE;
                try {
                    userTransaction.rollback();
                } finally {
                    tli.alreadyOnStack = null;
                }
            }
            ITransactionListener[] transactionListeners = transactionListenerProvider.getTransactionListeners();
            for (ITransactionListener transactionListener : transactionListeners) {
                try {
                    transactionListener.handlePostRollback(sessionId, fatalError);
                } catch (Exception e) {
                    throw RuntimeExceptionUtil.mask(e);
                }
            }
            long tillFlushTime = System.currentTimeMillis();
            if (eventDispatcher != null && !Boolean.TRUE.equals(readOnly)) {
                eventDispatcher.dispatchEvent(new DatabaseFailEvent(sessionId));
            }
            if (log.isDebugEnabled()) {
                long currTime = System.currentTimeMillis();
                long overall = currTime - openTime;
                long app = preRollbackTime - openTime;
                long preRollback = tillFlushTime - preRollbackTime;
                long revert = currTime - tillFlushTime;
                log.debug("Transaction rollback (overall // app / preRollback / revert): " + overall + " // " + app + " / " + preRollback + " / " + revert + " ms");
            }
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        } finally {
            databaseSessionIdController.releaseSessionId(sessionId);
        }
    }

    @Override
    public void processAndCommit(final DatabaseCallback databaseCallback) {
        processAndCommitWithResult(persistenceUnitToDatabaseMap -> {
            databaseCallback.callback(persistenceUnitToDatabaseMap);
            return null;
        });
    }

    @Override
    public void processAndCommit(final DatabaseCallback databaseCallback, boolean expectOwnDatabaseSession, boolean readOnly) {
        processAndCommitWithResult(persistenceUnitToDatabaseMap -> {
            databaseCallback.callback(persistenceUnitToDatabaseMap);
            return null;
        }, expectOwnDatabaseSession, readOnly);
    }

    @Override
    public <R> R processAndCommitWithResult(ResultingDatabaseCallback<R> databaseCallback) {
        return processAndCommit(databaseCallback, false, false, false);
    }

    @Override
    public <R> R processAndCommitWithResult(ResultingDatabaseCallback<R> databaseCallback, boolean expectOwnDatabaseSession, boolean readOnly) {
        return processAndCommit(databaseCallback, expectOwnDatabaseSession, readOnly, false);
    }

    @SneakyThrows
    public <R> R processAndCommit(ResultingDatabaseCallback<R> databaseCallback, boolean expectOwnDatabaseSession, boolean readOnly, boolean lazyTransaction) {
        readOnly = false;
        var tli = getEnsureTLI();
        if (isActive()) {
            if (expectOwnDatabaseSession) {
                throw new IllegalStateException("Transaction already active");
            }
            var databaseMap = Boolean.TRUE.equals(tli.beginInProgress) ? null : tli.databaseMap;
            return databaseCallback.callback(databaseMap);
        }
        if (lazyTransaction) {
            var oldLazyMode = tli.lazyMode;
            if (oldLazyMode) {
                // nothing to do. any success or error will be handled already in the outer scope
                return databaseCallback.callback(null);
            }
            tli.lazyMode = true;
            var success = false;
            Throwable recoverableException = null;
            try {
                R result = databaseCallback.callback(null);
                if (!isActive()) {
                    // during the callback no transaction has been opened & pending for close
                    // so we have nothing to do in this case
                    return result;
                }
                if (readOnly) {
                    rollback(false);
                } else {
                    commit();
                }
                success = true;
                return result;
            } catch (OptimisticLockException e) {
                recoverableException = e;
                throw e;
            } catch (PersistenceException | SQLException | Error e) {
                throw RuntimeExceptionUtil.mask(e);
            } catch (Throwable e) {
                // all other exceptions are assumed application based and therefore recoverable
                recoverableException = e;
                throw RuntimeExceptionUtil.mask(e);
            } finally {
                tli.lazyMode = false;
                if (!success) {
                    rollback(recoverableException == null);
                }
            }
        }
        if (tli.lazyMode) {
            // previous call to this JdbcTransaction with "lazy" flag. So we handle rollbacks/success at
            // the "previous" outer level
            try {
                begin(false); // intentionally open the transaction "writable" in any case if we are in lazy
                // mode
                var databaseMap = tli.databaseMap;
                return databaseCallback.callback(databaseMap);
            } catch (Exception e) {
                throw RuntimeExceptionUtil.mask(e);
            }
        }
        var success = false;
        Exception recoverableException = null;
        try {
            begin(readOnly);
            var databaseMap = tli.databaseMap;
            var result = databaseCallback.callback(databaseMap);
            if (readOnly) {
                rollback(false);
            } else {
                commit();
            }
            success = true;
            return result;
        } catch (OptimisticLockException e) {
            recoverableException = e;
            throw e;
        } finally {
            if (!success) {
                rollback(recoverableException == null);
            }
        }
    }

    @Override
    public boolean isActive() {
        return getTransactionInfo() != null;
    }

    @Override
    public void cleanupThreadLocal() {
        if (getTransactionInfo() == null) {
            tliTL.remove();
            return;
        }
        throw new UnsupportedOperationException("It is not supported to clean this ThreadLocal while you are in a transaction because this can lead to an inconsistent state");
    }

    @Override
    public Boolean isExternalTransactionManagerActive() {
        var tli = tliTL.get();
        if (tli == null) {
            return null;
        }
        return tli.etmActive;
    }

    @Override
    public void setExternalTransactionManagerActive(Boolean active) {
        ThreadLocalItem tli = null;
        if (active == null) {
            tli = tliTL.get();
            if (tli == null) {
                // Nothing to do in this case
                return;
            }
        }
        if (tli == null) {
            tli = getEnsureTLI();
        }
        tli.etmActive = active;
    }

    @Override
    public void runInTransaction(final CheckedRunnable runnable) {
        processAndCommit(persistenceUnitToDatabaseMap -> CheckedRunnable.invoke(runnable));
    }

    @Override
    public <R> R runInTransaction(final CheckedSupplier<R> runnable) {
        return processAndCommitWithResult(persistenceUnitToDatabaseMap -> CheckedSupplier.invoke(runnable));
    }

    @Override
    public void runInLazyTransaction(CheckedRunnable runnable) {
        processAndCommit(persistenceUnitToDatabaseMap -> {
            CheckedRunnable.invoke(runnable);
            return null;
        }, false, false, true);
    }

    @Override
    public <R> R runInLazyTransaction(final CheckedSupplier<R> runnable) {
        return processAndCommit(persistenceUnitToDatabaseMap -> {
            return CheckedSupplier.invoke(runnable);
        }, false, false, true);
    }

    @Override
    public void runOnTransactionPreCommit(CheckedRunnable runnable) {
        ThreadLocalItem tli = tliTL.get();
        if (tli == null || tli.sessionId == null) {
            throw new IllegalStateException("No transaction is currently active");
        }
        if (tli.preCommitRunnables == null) {
            tli.preCommitRunnables = new ArrayList<>();
        }
        tli.preCommitRunnables.add(runnable);
    }

    @Override
    public void runOnTransactionPostCommit(CheckedRunnable runnable) {
        ThreadLocalItem tli = tliTL.get();
        if (tli == null || tli.sessionId == null) {
            throw new IllegalStateException("No transaction is currently active");
        }
        if (tli.postCommitRunnables == null) {
            tli.postCommitRunnables = new ArrayList<>();
        }
        tli.postCommitRunnables.add(runnable);
    }

    public static class ThreadLocalItem implements ITransactionInfo {
        public Boolean ignoreReleaseDatabase;

        public Boolean alreadyOnStack;

        public Long sessionId;

        public Boolean beginInProgress;

        public Boolean isReadOnly;

        public Boolean etmActive;

        public LinkedHashMap<Object, IDatabase> databaseMap;

        public ArrayList<CheckedRunnable> preCommitRunnables;

        public ArrayList<CheckedRunnable> postCommitRunnables;

        public boolean lazyMode;

        public long openTime;

        @Override
        public long getSessionId() {
            return sessionId != null ? sessionId.longValue() : 0;
        }

        @Override
        public boolean isReadOnly() {
            return isReadOnly != null ? isReadOnly.booleanValue() : false;
        }
    }
}
