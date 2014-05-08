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
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.datachange.model.IDataChangeEntry;
import de.osthus.ambeth.event.IEventListener;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.query.IQueryKey;
import de.osthus.ambeth.util.IParamHolder;
import de.osthus.ambeth.util.ParamChecker;

public class QueryResultCache implements IInitializingBean, IEventListener, IQueryResultCache
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final HashMap<IQueryKey, Reference<IQueryResultCacheItem>> queryKeyToObjRefMap = new HashMap<IQueryKey, Reference<IQueryResultCacheItem>>();

	protected final HashMap<Class<?>, ILinkedSet<IQueryKey>> entityTypeToQueryKeyMap = new HashMap<Class<?>, ILinkedSet<IQueryKey>>();

	protected final Lock readLock, writeLock;

	protected final HashSet<IQueryKey> pendingQueryKeysSet = new HashSet<IQueryKey>(0.5f);

	protected boolean queryCacheActive = true;

	protected final Condition pendingQueryKeysChangedCond;

	public QueryResultCache()
	{
		ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
		readLock = rwLock.readLock();
		writeLock = rwLock.writeLock();
		pendingQueryKeysChangedCond = rwLock.writeLock().newCondition();
	}

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
	}

	@Property(name = PersistenceConfigurationConstants.QueryCacheActive, defaultValue = "true")
	public void setQueryCacheActive(boolean queryCacheActive)
	{
		this.queryCacheActive = queryCacheActive;
	}

	@Override
	public IQueryResultCacheItem getCacheItem(IQueryKey queryKey)
	{
		Lock readLock = this.readLock;
		readLock.lock();
		try
		{
			Reference<IQueryResultCacheItem> sr = queryKeyToObjRefMap.get(queryKey);
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

			Lock writeLock = this.writeLock;
			writeLock.lock();
			try
			{
				queryKeyToObjRefMap.put(queryKey, new SoftReference<IQueryResultCacheItem>(queryResultCacheItem));

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
		ArrayList<IObjRef> resultList = new ArrayList<IObjRef>();
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
		for (int a = offset, size = offset + length; a < size; a++)
		{
			resultList.add(queryResultCacheItem.getObjRef(a, idIndex));
		}
		totalSize.setValue(Integer.valueOf(cachedTotalSize));
		return resultList;
	}

	@Override
	public void handleEvent(Object eventObject, long dispatchTime, long sequenceId)
	{
		if (eventObject instanceof ClearAllCachesEvent)
		{
			Lock writeLock = this.writeLock;
			writeLock.lock();
			try
			{
				entityTypeToQueryKeyMap.clear();
				queryKeyToObjRefMap.clear();
			}
			finally
			{
				writeLock.unlock();
			}
			return;
		}
		if (!(eventObject instanceof IDataChange))
		{
			return;
		}
		IDataChange dataChange = (IDataChange) eventObject;
		if (dataChange.isEmpty())
		{
			return;
		}
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

		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			for (Class<?> entityType : occuringTypes)
			{
				removeCacheItemsRelatedToEntityType(entityType);
			}
			removeCacheItemsRelatedToEntityType(Object.class);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected void removeCacheItemsRelatedToEntityType(Class<?> entityType)
	{
		ILinkedSet<IQueryKey> queryKeysRelatedToEntityType = entityTypeToQueryKeyMap.remove(entityType);
		if (queryKeysRelatedToEntityType == null)
		{
			return;
		}
		HashMap<IQueryKey, Reference<IQueryResultCacheItem>> queryKeyToObjRefMap = this.queryKeyToObjRefMap;
		for (IQueryKey queryKeyRelatedToEntityType : queryKeysRelatedToEntityType)
		{
			queryKeyToObjRefMap.remove(queryKeyRelatedToEntityType);
		}
	}
}
