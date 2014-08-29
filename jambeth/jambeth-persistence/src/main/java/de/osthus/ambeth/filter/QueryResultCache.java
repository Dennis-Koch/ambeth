package de.osthus.ambeth.filter;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.ILinkedSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.database.ITransactionInfo;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.datachange.model.IDataChangeEntry;
import de.osthus.ambeth.datachange.model.IDataChangeOfSession;
import de.osthus.ambeth.event.IDatabaseReleaseEvent;
import de.osthus.ambeth.event.IEventListener;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.query.IQueryKey;
import de.osthus.ambeth.util.IParamHolder;
import de.osthus.ambeth.util.ParamChecker;

public class QueryResultCache implements IQueryResultCache, IEventListener
{
	public static class QueryResultCacheSession
	{
		protected final HashMap<IQueryKey, Reference<IQueryResultCacheItem>> queryKeyToObjRefMap = new HashMap<IQueryKey, Reference<IQueryResultCacheItem>>();

		protected final long sessionId;

		public QueryResultCacheSession(long sessionId)
		{
			this.sessionId = sessionId;
		}

		public void clear()
		{
			queryKeyToObjRefMap.clear();
		}
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ITransaction transaction;

	@Property(name = PersistenceConfigurationConstants.QueryCacheActive, defaultValue = "true")
	protected boolean queryCacheActive = true;

	protected final HashMap<IQueryKey, Reference<IQueryResultCacheItem>> queryKeyToObjRefMap = new HashMap<IQueryKey, Reference<IQueryResultCacheItem>>();

	protected final HashMap<Class<?>, ILinkedSet<IQueryKey>> entityTypeToQueryKeyMap = new HashMap<Class<?>, ILinkedSet<IQueryKey>>();

	protected final HashMap<Long, QueryResultCacheSession> sessionIdToHandleMap = new HashMap<Long, QueryResultCacheSession>();

	protected final Lock readLock, writeLock;

	protected final HashSet<IQueryKey> pendingQueryKeysSet = new HashSet<IQueryKey>(0.5f);

	protected final Condition pendingQueryKeysChangedCond;

	public QueryResultCache()
	{
		ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
		readLock = rwLock.readLock();
		writeLock = rwLock.writeLock();
		pendingQueryKeysChangedCond = rwLock.writeLock().newCondition();
	}

	@Override
	public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) throws Exception
	{
		throw new UnsupportedOperationException("Should never be called");
	}

	@Override
	public IQueryResultCacheItem getCacheItem(IQueryKey queryKey)
	{
		ITransactionInfo transactionInfo = transaction.getTransactionInfo();
		Lock readLock = this.readLock;
		readLock.lock();
		try
		{
			Reference<IQueryResultCacheItem> sr = null;
			if (transactionInfo == null || transactionInfo.isReadOnly())
			{
				// go directly to the committed cache info
				sr = queryKeyToObjRefMap.get(queryKey);
			}
			else
			{
				QueryResultCacheSession session = sessionIdToHandleMap.get(Long.valueOf(transactionInfo.getSessionId()));
				if (session != null)
				{
					sr = session.queryKeyToObjRefMap.get(queryKey);
				}
			}
			return sr != null ? sr.get() : null;
		}
		finally
		{
			readLock.unlock();
		}
	}

	@Override
	public IList<IObjRef> getQueryResult(IQueryKey queryKey, IQueryResultRetriever queryResultRetriever, byte idIndex, int offset, int length,
			IParamHolder<Integer> totalSize)
	{
		ParamChecker.assertParamNotNull(queryKey, "queryKey");
		ParamChecker.assertParamNotNull(queryResultRetriever, "queryResultRetriever");
		boolean containsPageOnly = queryResultRetriever.containsPageOnly();
		if (!queryCacheActive || containsPageOnly)
		{
			// A retriever which only selects a specific page can currently not be cached
			IQueryResultCacheItem queryResultCacheItem = queryResultRetriever.getQueryResult();
			return createResult(queryResultCacheItem, idIndex, offset, length, containsPageOnly, totalSize);
		}
		IQueryResultCacheItem queryResultCacheItem = getCacheItem(queryKey);
		if (queryResultCacheItem != null)
		{
			return createResult(queryResultCacheItem, idIndex, offset, length, containsPageOnly, totalSize);
		}
		queryResultCacheItem = getCacheItemWait(queryKey);
		if (queryResultCacheItem != null)
		{
			return createResult(queryResultCacheItem, idIndex, offset, length, containsPageOnly, totalSize);
		}
		try
		{
			List<Class<?>> relatedEntityTypes = queryResultRetriever.getRelatedEntityTypes();
			queryResultCacheItem = queryResultRetriever.getQueryResult();

			ITransactionInfo transactionInfo = transaction.getTransactionInfo();
			Lock writeLock = this.writeLock;
			writeLock.lock();
			try
			{
				if (transactionInfo != null && !transactionInfo.isReadOnly())
				{
					Long sessionId = Long.valueOf(transactionInfo.getSessionId());
					QueryResultCacheSession session = sessionIdToHandleMap.get(sessionId);
					if (session == null)
					{
						session = new QueryResultCacheSession(sessionId.longValue());
						sessionIdToHandleMap.put(sessionId, session);
					}
					session.queryKeyToObjRefMap.put(queryKey, new SoftReference<IQueryResultCacheItem>(queryResultCacheItem));
				}
				else
				{
					queryKeyToObjRefMap.put(queryKey, new SoftReference<IQueryResultCacheItem>(queryResultCacheItem));
				}

				if (relatedEntityTypes != null)
				{
					for (int a = relatedEntityTypes.size(); a-- > 0;)
					{
						Class<?> relatedEntityType = relatedEntityTypes.get(a);
						registerQueryKeyWithEntityType(relatedEntityType, queryKey);
					}
				}
				else
				{
					// null relatedEntityTypes means "unknown". In this case invalidate the query key whenever
					// ANY datachange occurs on any entity
					registerQueryKeyWithEntityType(Object.class, queryKey);
				}
			}
			finally
			{
				writeLock.unlock();
			}
		}
		finally
		{
			notifyWaiting(queryKey);
		}
		return createResult(queryResultCacheItem, idIndex, offset, length, containsPageOnly, totalSize);
	}

	private void notifyWaiting(IQueryKey queryKey)
	{
		HashSet<IQueryKey> pendingQueryKeysSet = this.pendingQueryKeysSet;
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			pendingQueryKeysSet.remove(queryKey);
			pendingQueryKeysChangedCond.signalAll();
		}
		finally
		{
			writeLock.unlock();
		}
	}

	private IQueryResultCacheItem getCacheItemWait(IQueryKey queryKey)
	{
		HashSet<IQueryKey> pendingQueryKeysSet = this.pendingQueryKeysSet;
		Date waitTill = new Date(System.currentTimeMillis() + 10 * 60 * 1000); // 10 mins
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			IQueryResultCacheItem queryResultCacheItem = getCacheItem(queryKey);
			if (queryResultCacheItem == null)
			{
				while (!pendingQueryKeysSet.add(queryKey))
				{
					try
					{
						if (!pendingQueryKeysChangedCond.awaitUntil(waitTill))
						{
							return null;
						}
					}
					catch (InterruptedException e)
					{
						throw RuntimeExceptionUtil.mask(e);
					}
					queryResultCacheItem = getCacheItem(queryKey);
					if (queryResultCacheItem != null)
					{
						break;
					}
				}
			}
			return queryResultCacheItem;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected void registerQueryKeyWithEntityType(Class<?> entityType, IQueryKey queryKey)
	{
		ILinkedSet<IQueryKey> queryKeys = entityTypeToQueryKeyMap.get(entityType);
		if (queryKeys == null)
		{
			queryKeys = new LinkedHashSet<IQueryKey>();
			entityTypeToQueryKeyMap.put(entityType, queryKeys);
		}
		queryKeys.add(queryKey);
	}

	protected IList<IObjRef> createResult(IQueryResultCacheItem queryResultCacheItem, byte idIndex, int offset, int length, boolean containsPageOnly,
			IParamHolder<Integer> totalSize)
	{
		int cachedTotalSize = queryResultCacheItem.getSize();
		if (containsPageOnly)
		{
			offset = 0;
			length = queryResultCacheItem.getSize();
		}
		else
		{
			if (length < 0)
			{
				length = cachedTotalSize - offset;
			}
			else
			{
				length = Math.min(cachedTotalSize - offset, length);
			}
		}
		ArrayList<IObjRef> resultList = new ArrayList<IObjRef>(length);
		for (int a = offset, size = offset + length; a < size; a++)
		{
			resultList.add(queryResultCacheItem.getObjRef(a, idIndex));
		}
		totalSize.setValue(Integer.valueOf(cachedTotalSize));
		return resultList;
	}

	protected ISet<Class<?>> collectOccuringTypes(IDataChange dataChange)
	{
		LinkedHashSet<Class<?>> occuringTypes = new LinkedHashSet<Class<?>>();

		List<IDataChangeEntry> deletes = dataChange.getDeletes();
		List<IDataChangeEntry> updates = dataChange.getUpdates();
		List<IDataChangeEntry> inserts = dataChange.getInserts();
		for (int a = deletes.size(); a-- > 0;)
		{
			Class<?> entityType = deletes.get(a).getEntityType();
			if (entityType == null)
			{
				continue;
			}
			occuringTypes.add(entityType);
		}
		for (int a = updates.size(); a-- > 0;)
		{
			Class<?> entityType = updates.get(a).getEntityType();
			if (entityType == null)
			{
				continue;
			}
			occuringTypes.add(entityType);
		}
		for (int a = inserts.size(); a-- > 0;)
		{
			Class<?> entityType = inserts.get(a).getEntityType();
			if (entityType == null)
			{
				continue;
			}
			occuringTypes.add(entityType);
		}
		return occuringTypes;
	}

	public void handleDataChange(IDataChange dataChange)
	{
		handleDataChangeIntern(dataChange, queryKeyToObjRefMap);
	}

	public void handleDatabaseRelease(IDatabaseReleaseEvent event)
	{
		writeLock.lock();
		try
		{
			sessionIdToHandleMap.remove(Long.valueOf(event.getSessionId()));
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected void handleDataChangeIntern(IDataChange dataChange, HashMap<IQueryKey, Reference<IQueryResultCacheItem>> queryKeyToObjRefMap)
	{
		if (dataChange.isEmpty())
		{
			return;
		}
		ISet<Class<?>> occuringTypes = collectOccuringTypes(dataChange);
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			for (Class<?> entityType : occuringTypes)
			{
				removeCacheItemsRelatedToEntityType(entityType, queryKeyToObjRefMap);
			}
			removeCacheItemsRelatedToEntityType(Object.class, queryKeyToObjRefMap);
		}
		finally
		{
			writeLock.unlock();
		}

	}

	public void handleClearAllCaches(ClearAllCachesEvent event)
	{
		ITransactionInfo transactionInfo = transaction.getTransactionInfo();
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			if (transactionInfo != null)
			{
				QueryResultCacheSession session = sessionIdToHandleMap.get(Long.valueOf(transactionInfo.getSessionId()));
				if (session != null)
				{
					session.clear();
				}
			}
			entityTypeToQueryKeyMap.clear();
			queryKeyToObjRefMap.clear();
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public void handleDataChangeOfSession(IDataChangeOfSession dataChangeOfSession)
	{
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			QueryResultCacheSession session = sessionIdToHandleMap.get(Long.valueOf(dataChangeOfSession.getSessionId()));
			if (session == null)
			{
				// nothing to do
				return;
			}
			handleDataChangeIntern(dataChangeOfSession.getDataChange(), session.queryKeyToObjRefMap);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected void removeCacheItemsRelatedToEntityType(Class<?> entityType, HashMap<IQueryKey, Reference<IQueryResultCacheItem>> queryKeyToObjRefMap)
	{
		ILinkedSet<IQueryKey> queryKeysRelatedToEntityType = entityTypeToQueryKeyMap.remove(entityType);
		if (queryKeysRelatedToEntityType == null)
		{
			return;
		}
		for (IQueryKey queryKeyRelatedToEntityType : queryKeysRelatedToEntityType)
		{
			queryKeyToObjRefMap.remove(queryKeyRelatedToEntityType);
		}
	}
}
