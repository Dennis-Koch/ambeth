package com.koch.ambeth.persistence.filter;

/*-
 * #%L
 * jambeth-persistence
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

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.datachange.model.IDataChangeEntry;
import com.koch.ambeth.datachange.model.IDataChangeOfSession;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.api.database.ITransaction;
import com.koch.ambeth.persistence.api.database.ITransactionInfo;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.persistence.event.IDatabaseReleaseEvent;
import com.koch.ambeth.query.IQueryKey;
import com.koch.ambeth.query.filter.IQueryResultCache;
import com.koch.ambeth.query.filter.IQueryResultCacheItem;
import com.koch.ambeth.query.filter.IQueryResultRetriever;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.IParamHolder;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.ILinkedSet;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class QueryResultCache implements IQueryResultCache {
    protected final HashMap<IQueryKey, Reference<IQueryResultCacheItem>> queryKeyToObjRefMap = new HashMap<>();
    protected final HashMap<Class<?>, ILinkedSet<IQueryKey>> entityTypeToQueryKeyMap = new HashMap<>();
    protected final HashMap<Long, QueryResultCacheSession> sessionIdToHandleMap = new HashMap<>();
    protected final Lock readLock, writeLock;
    protected final HashSet<IQueryKey> pendingQueryKeysSet = new HashSet<>(0.5f);
    protected final Condition pendingQueryKeysChangedCond;
    @Autowired
    protected ITransaction transaction;
    @Property(name = PersistenceConfigurationConstants.QueryCacheActive, defaultValue = "true")
    protected boolean queryCacheActive = true;
    @LogInstance
    private ILogger log;

    public QueryResultCache() {
        ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        readLock = rwLock.readLock();
        writeLock = rwLock.writeLock();
        pendingQueryKeysChangedCond = rwLock.writeLock().newCondition();
    }

    public IQueryResultCacheItem getCacheItem(IQueryKey queryKey, int offset, int length, boolean containsPageOnly) {
        ITransactionInfo transactionInfo = transaction.getTransactionInfo();
        Lock readLock = this.readLock;
        readLock.lock();
        try {
            Reference<IQueryResultCacheItem> sr = null;
            if (transactionInfo == null || transactionInfo.isReadOnly()) {
                // go directly to the committed cache info
                // if (containsPageOnly) {
                // sr = queryKeyWithPageToObjRefMap.get(queryKey, offset, length);
                // }
                // else {
                sr = queryKeyToObjRefMap.get(queryKey);
                // }
            } else {
                QueryResultCacheSession session = sessionIdToHandleMap.get(Long.valueOf(transactionInfo.getSessionId()));
                if (session != null) {
                    sr = session.queryKeyToObjRefMap.get(queryKey);
                }
            }
            return sr != null ? sr.get() : null;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<IObjRef> getQueryResult(IQueryKey queryKey, IQueryResultRetriever queryResultRetriever, byte idIndex, int offset, int length, IParamHolder<Long> totalSize) {
        ParamChecker.assertParamNotNull(queryKey, "queryKey");
        ParamChecker.assertParamNotNull(queryResultRetriever, "queryResultRetriever");
        var containsPageOnly = queryResultRetriever.containsPageOnly();
        if (!queryCacheActive) {
            // A retriever which only selects a specific page can currently not be cached
            IQueryResultCacheItem queryResultCacheItem = queryResultRetriever.getQueryResult();
            return createResult(queryResultCacheItem, idIndex, offset, length, containsPageOnly, totalSize);
        }
        var queryResultCacheItem = getCacheItem(queryKey, offset, length, containsPageOnly);
        if (queryResultCacheItem != null) {
            return createResult(queryResultCacheItem, idIndex, offset, length, containsPageOnly, totalSize);
        }
        queryResultCacheItem = getCacheItemWait(queryKey, offset, length, containsPageOnly);
        if (queryResultCacheItem != null) {
            return createResult(queryResultCacheItem, idIndex, offset, length, containsPageOnly, totalSize);
        }
        try {
            queryResultCacheItem = queryResultRetriever.getQueryResult();
            if (length > 0) {
                List<Class<?>> relatedEntityTypes = queryResultRetriever.getRelatedEntityTypes();

                ITransactionInfo transactionInfo = transaction.getTransactionInfo();
                Lock writeLock = this.writeLock;
                writeLock.lock();
                try {
                    if (transactionInfo != null && !transactionInfo.isReadOnly()) {
                        Long sessionId = Long.valueOf(transactionInfo.getSessionId());
                        QueryResultCacheSession session = sessionIdToHandleMap.get(sessionId);
                        if (session == null) {
                            session = new QueryResultCacheSession(sessionId.longValue());
                            sessionIdToHandleMap.put(sessionId, session);
                        }
                        session.queryKeyToObjRefMap.put(queryKey, new SoftReference<>(queryResultCacheItem));
                    } else {
                        queryKeyToObjRefMap.put(queryKey, new SoftReference<>(queryResultCacheItem));
                    }
                    if (relatedEntityTypes != null) {
                        for (int a = relatedEntityTypes.size(); a-- > 0; ) {
                            Class<?> relatedEntityType = relatedEntityTypes.get(a);
                            registerQueryKeyWithEntityType(relatedEntityType, queryKey);
                        }
                    } else {
                        // null relatedEntityTypes means "unknown". In this case invalidate the query key
                        // whenever ANY datachange occurs on any entity
                        registerQueryKeyWithEntityType(Object.class, queryKey);
                    }
                } finally {
                    writeLock.unlock();
                }
            }
        } finally {
            notifyWaiting(queryKey);
        }
        return createResult(queryResultCacheItem, idIndex, offset, length, containsPageOnly, totalSize);
    }

    private void notifyWaiting(IQueryKey queryKey) {
        HashSet<IQueryKey> pendingQueryKeysSet = this.pendingQueryKeysSet;
        Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            pendingQueryKeysSet.remove(queryKey);
            pendingQueryKeysChangedCond.signalAll();
        } finally {
            writeLock.unlock();
        }
    }

    private IQueryResultCacheItem getCacheItemWait(IQueryKey queryKey, int offset, int length, boolean containsPageOnly) {
        if (length == 0) {
            return null;
        }
        HashSet<IQueryKey> pendingQueryKeysSet = this.pendingQueryKeysSet;
        Date waitTill = new Date(System.currentTimeMillis() + 10 * 60 * 1000); // 10 mins
        Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            IQueryResultCacheItem queryResultCacheItem = getCacheItem(queryKey, offset, length, containsPageOnly);
            if (queryResultCacheItem != null) {
                return queryResultCacheItem;
            }
            while (!pendingQueryKeysSet.add(queryKey)) {
                try {
                    if (!pendingQueryKeysChangedCond.awaitUntil(waitTill)) {
                        return null;
                    }
                } catch (InterruptedException e) {
                    throw RuntimeExceptionUtil.mask(e);
                }
                queryResultCacheItem = getCacheItem(queryKey, offset, length, containsPageOnly);
                if (queryResultCacheItem != null) {
                    return queryResultCacheItem;
                }
            }
            return null;
        } finally {
            writeLock.unlock();
        }
    }

    protected void registerQueryKeyWithEntityType(Class<?> entityType, IQueryKey queryKey) {
        ILinkedSet<IQueryKey> queryKeys = entityTypeToQueryKeyMap.get(entityType);
        if (queryKeys == null) {
            queryKeys = new LinkedHashSet<>();
            entityTypeToQueryKeyMap.put(entityType, queryKeys);
        }
        queryKeys.add(queryKey);
    }

    protected List<IObjRef> createResult(IQueryResultCacheItem queryResultCacheItem, byte idIndex, int offset, int length, boolean containsPageOnly, IParamHolder<Long> totalSize) {
        long cachedTotalSize = queryResultCacheItem.getTotalSize();
        if (containsPageOnly) {
            offset = 0;
            length = queryResultCacheItem.getSize();
        } else {
            if (length < 0) {
                length = (int) (cachedTotalSize - offset);
            } else {
                length = (int) Math.min(cachedTotalSize - offset, length);
            }
        }
        if (length < 0) {
            length = 0;
        }
        ArrayList<IObjRef> resultList = new ArrayList<>(length);
        for (int a = offset, size = offset + length; a < size; a++) {
            resultList.add(queryResultCacheItem.getObjRef(a, idIndex));
        }
        totalSize.setValue(Long.valueOf(cachedTotalSize));
        return resultList;
    }

    protected ISet<Class<?>> collectOccuringTypes(IDataChange dataChange) {
        LinkedHashSet<Class<?>> occuringTypes = new LinkedHashSet<>();

        List<IDataChangeEntry> deletes = dataChange.getDeletes();
        List<IDataChangeEntry> updates = dataChange.getUpdates();
        List<IDataChangeEntry> inserts = dataChange.getInserts();
        for (int a = deletes.size(); a-- > 0; ) {
            Class<?> entityType = deletes.get(a).getEntityType();
            if (entityType == null) {
                continue;
            }
            occuringTypes.add(entityType);
        }
        for (int a = updates.size(); a-- > 0; ) {
            Class<?> entityType = updates.get(a).getEntityType();
            if (entityType == null) {
                continue;
            }
            occuringTypes.add(entityType);
        }
        for (int a = inserts.size(); a-- > 0; ) {
            Class<?> entityType = inserts.get(a).getEntityType();
            if (entityType == null) {
                continue;
            }
            occuringTypes.add(entityType);
        }
        return occuringTypes;
    }

    public void handleDataChange(IDataChange dataChange) {
        handleDataChangeIntern(dataChange, queryKeyToObjRefMap);
    }

    public void handleDatabaseRelease(IDatabaseReleaseEvent event) {
        writeLock.lock();
        try {
            sessionIdToHandleMap.remove(Long.valueOf(event.getSessionId()));
        } finally {
            writeLock.unlock();
        }
    }

    protected void handleDataChangeIntern(IDataChange dataChange, HashMap<IQueryKey, Reference<IQueryResultCacheItem>> queryKeyToObjRefMap) {
        if (dataChange.isEmpty()) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("processing data change to invalidate corresponding query cache entries of committed state");
        }
        ISet<Class<?>> occuringTypes = collectOccuringTypes(dataChange);
        Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            for (Class<?> entityType : occuringTypes) {
                removeCacheItemsRelatedToEntityType(entityType, queryKeyToObjRefMap);
            }
            removeCacheItemsRelatedToEntityType(Object.class, queryKeyToObjRefMap);
        } finally {
            writeLock.unlock();
        }
        if (log.isDebugEnabled()) {
            log.debug("processed data change to invalidate corresponding query cache entries of committed state");
        }
    }

    public void handleClearAllCaches(ClearAllCachesEvent event) {
        ITransactionInfo transactionInfo = transaction.getTransactionInfo();
        Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            if (transactionInfo != null) {
                QueryResultCacheSession session = sessionIdToHandleMap.get(Long.valueOf(transactionInfo.getSessionId()));
                if (session != null) {
                    session.clear();
                }
            }
            if (!entityTypeToQueryKeyMap.isEmpty() || !queryKeyToObjRefMap.isEmpty()) {
                entityTypeToQueryKeyMap.clear();
                queryKeyToObjRefMap.clear();
                if (log.isDebugEnabled()) {
                    log.debug("cleared query cache of committed state");
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void handleDataChangeOfSession(IDataChangeOfSession dataChangeOfSession) {
        Lock writeLock = this.writeLock;
        writeLock.lock();
        try {
            QueryResultCacheSession session = sessionIdToHandleMap.get(Long.valueOf(dataChangeOfSession.getSessionId()));
            if (session == null) {
                // nothing to do
                return;
            }
            IDataChange dataChange = dataChangeOfSession.getDataChange();
            if (dataChange.isEmpty()) {
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("processing data change to invalidate corresponding query cache entries of transaction '" + dataChangeOfSession.getSessionId() + "'");
            }
            handleDataChangeIntern(dataChange, session.queryKeyToObjRefMap);
            if (log.isDebugEnabled()) {
                log.debug("processed data change to invalidate corresponding query cache entries of transaction '" + dataChangeOfSession.getSessionId() + "'");
            }
        } finally {
            writeLock.unlock();
        }
    }

    protected void removeCacheItemsRelatedToEntityType(Class<?> entityType, HashMap<IQueryKey, Reference<IQueryResultCacheItem>> queryKeyToObjRefMap) {
        ILinkedSet<IQueryKey> queryKeysRelatedToEntityType = entityTypeToQueryKeyMap.remove(entityType);
        if (queryKeysRelatedToEntityType == null) {
            return;
        }
        for (IQueryKey queryKeyRelatedToEntityType : queryKeysRelatedToEntityType) {
            queryKeyToObjRefMap.remove(queryKeyRelatedToEntityType);
        }
    }

    public class QueryResultCacheSession {
        protected final HashMap<IQueryKey, Reference<IQueryResultCacheItem>> queryKeyToObjRefMap = new HashMap<>();

        protected final long sessionId;

        public QueryResultCacheSession(long sessionId) {
            this.sessionId = sessionId;
        }

        public void clear() {
            if (queryKeyToObjRefMap.isEmpty()) {
                return;
            }
            queryKeyToObjRefMap.clear();
            if (log.isDebugEnabled()) {
                log.debug("cleared query cache of transaction '" + sessionId + "'");
            }
        }
    }
}
