package de.osthus.ambeth.cache;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.ITransactionState;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.StringBuilderUtil;

public class FirstLevelCacheManager implements IInitializingBean, IFirstLevelCacheExtendable, IFirstLevelCacheManager
{
	@LogInstance
	private ILogger log;

	protected final LinkedHashMap<Integer, FlcEntry> allFLCs = new LinkedHashMap<Integer, FlcEntry>();

	protected int changeCount, lastCacheId;

	protected IThreadLocalObjectCollector objectCollector;

	protected ITransactionState transactionState;

	protected final Lock unboundReadLock, unboundWriteLock;

	public FirstLevelCacheManager()
	{
		ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
		unboundReadLock = rwLock.readLock();
		unboundWriteLock = rwLock.writeLock();
	}

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(objectCollector, "ObjectCollector");
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	public void setTransactionState(ITransactionState transactionState)
	{
		this.transactionState = transactionState;
	}

	protected <V> Reference<V> createReferenceEntry(V firstLevelCache)
	{
		return new WeakReference<V>(firstLevelCache);
	}

	protected void cleanupCaches()
	{
		Iterator<Entry<Integer, FlcEntry>> iter = allFLCs.iterator();
		while (iter.hasNext())
		{
			Entry<Integer, FlcEntry> entry = iter.next();
			Integer cacheId = entry.getKey();
			FlcEntry flcEntry = entry.getValue();
			IWritableCache writableCache = flcEntry.getFirstLevelCache();
			if (writableCache != null)
			{
				continue;
			}
			iter.remove();
			if (log.isDebugEnabled())
			{
				log.debug(StringBuilderUtil.concat(objectCollector.getCurrent(), "GCed first level cache with id: ", cacheId));
			}
		}
	}

	protected void checkCachesForCleanup()
	{
		changeCount++;
		if (changeCount >= 1000)
		{
			cleanupCaches();
			changeCount = 0;
		}
	}

	@Override
	public void registerFirstLevelCache(IWritableCache firstLevelCache, CacheFactoryDirective cacheFactoryDirective, boolean foreignThreadAware)
	{
		Reference<IWritableCache> firstLevelCacheR = createReferenceEntry(firstLevelCache);
		Thread thread = Thread.currentThread();
		Reference<Thread> owningThreadR = !foreignThreadAware ? createReferenceEntry(thread) : null;
		FlcEntry flcEntry = new FlcEntry(cacheFactoryDirective, firstLevelCacheR, owningThreadR);
		Lock unboundWriteLock = this.unboundWriteLock;
		unboundWriteLock.lock();
		try
		{
			Integer cacheId;
			int nextCacheId = lastCacheId;
			while (true)
			{
				if (nextCacheId == Integer.MAX_VALUE)
				{
					nextCacheId = 0;
				}
				cacheId = new Integer(++nextCacheId);

				if (allFLCs.putIfNotExists(cacheId, flcEntry))
				{
					lastCacheId = cacheId;
					break;
				}
			}
			firstLevelCache.setCacheId(cacheId);
			if (log.isDebugEnabled())
			{
				String foreignThreadText;
				if (foreignThreadAware)
				{
					foreignThreadText = ", multithreaded";
				}
				else
				{
					foreignThreadText = StringBuilderUtil.concat(objectCollector.getCurrent(), ", to thread ", thread.getId(), ":", thread.getName());
				}
				String privilegedText;
				if (firstLevelCache.isPrivileged())
				{
					privilegedText = ", privileged";
				}
				else
				{
					privilegedText = ", non-privileged";
				}
				if (CacheFactoryDirective.SubscribeTransactionalDCE.equals(flcEntry.getCacheFactoryDirective()))
				{
					log.debug(StringBuilderUtil.concat(objectCollector.getCurrent(), "Registered FLC with id: ", firstLevelCache.getCacheId(), privilegedText,
							", transactional", foreignThreadText));
				}
				else if (CacheFactoryDirective.SubscribeGlobalDCE.equals(flcEntry.getCacheFactoryDirective()))
				{
					log.debug(StringBuilderUtil.concat(objectCollector.getCurrent(), "Registered FLC with id: ", firstLevelCache.getCacheId(), privilegedText,
							", non-transactional", foreignThreadText));
				}
				else
				{
					log.debug(StringBuilderUtil.concat(objectCollector.getCurrent(), "Registered FLC with id: ", firstLevelCache.getCacheId(), privilegedText,
							", traced"));
				}
			}
		}
		finally
		{
			unboundWriteLock.unlock();
		}
	}

	@Override
	public void unregisterFirstLevelCache(IWritableCache firstLevelCache, CacheFactoryDirective cacheFactoryDirective, boolean foreignThreadAware)
	{
		// cacheFactoryDirective and foreignThreadAware will be intentionally ignored at unregister

		Lock unboundWriteLock = this.unboundWriteLock;
		unboundWriteLock.lock();
		try
		{
			checkCachesForCleanup();
			int cacheId = firstLevelCache.getCacheId();
			FlcEntry flcEntry = allFLCs.get(cacheId);
			if (flcEntry == null)
			{
				throw new IllegalStateException("CacheId is not mapped to a valid cache instance");
			}
			IWritableCache existingChildCache = flcEntry.getFirstLevelCache();
			if (existingChildCache == null)
			{
				throw new IllegalStateException("Fatal error occured. Reference lost to cache");
			}
			if (existingChildCache != firstLevelCache)
			{
				throw new IllegalStateException("Fatal error occured. CacheId invalid - it is not mapped to the specified cache instance");
			}
			allFLCs.remove(cacheId);
			foreignThreadAware = flcEntry.isForeignThreadAware();
			cacheFactoryDirective = flcEntry.getCacheFactoryDirective();
			if (log.isDebugEnabled())
			{
				String foreignThreadText;
				if (foreignThreadAware)
				{
					foreignThreadText = ", multithreaded";
				}
				else
				{
					Thread thread = flcEntry.getOwningThread();
					if (thread == null)
					{
						foreignThreadText = ", from unknown thread";
					}
					else
					{
						foreignThreadText = ", from thread " + thread.getId() + ":" + thread.getName();
					}
				}
				if (CacheFactoryDirective.SubscribeTransactionalDCE.equals(flcEntry.getCacheFactoryDirective()))
				{
					log.debug(StringBuilderUtil.concat(objectCollector.getCurrent(), "Unregistered FLC with id: ", firstLevelCache.getCacheId(),
							", transactional", foreignThreadText));
				}
				else if (CacheFactoryDirective.SubscribeGlobalDCE.equals(flcEntry.getCacheFactoryDirective()))
				{
					log.debug(StringBuilderUtil.concat(objectCollector.getCurrent(), "Unregistered FLC with id: ", firstLevelCache.getCacheId(),
							", non-transactional", foreignThreadText));
				}
				else
				{
					log.debug(StringBuilderUtil.concat(objectCollector.getCurrent(), "Unregistered FLC with id: ", firstLevelCache.getCacheId(), " traced"));
				}
			}
			firstLevelCache.setCacheId(0);
		}
		finally
		{
			unboundWriteLock.unlock();
		}
	}

	@Override
	public IList<IWritableCache> selectFirstLevelCaches()
	{
		boolean isTransactionActive = false;
		if (transactionState != null)
		{
			isTransactionActive = transactionState.isTransactionActive();
		}
		ArrayList<IWritableCache> liveChildCaches = new ArrayList<IWritableCache>();

		Lock unboundReadLock = this.unboundReadLock;
		unboundReadLock.lock();
		try
		{
			addLiveFirstLevelCaches(liveChildCaches, isTransactionActive);
		}
		finally
		{
			unboundReadLock.unlock();
		}
		return liveChildCaches;
	}

	protected void addLiveFirstLevelCaches(IList<IWritableCache> liveChildCaches, boolean isTransactionActive)
	{
		if (allFLCs.size() == 0)
		{
			return;
		}
		Thread currentThread = Thread.currentThread();
		for (Entry<Integer, FlcEntry> entry : allFLCs)
		{
			FlcEntry flcEntry = entry.getValue();
			CacheFactoryDirective cacheFactoryDirective = flcEntry.getCacheFactoryDirective();
			if (cacheFactoryDirective == null || CacheFactoryDirective.NoDCE.equals(cacheFactoryDirective))
			{
				// This cache is not interested in DCEs at all
				continue;
			}
			if (isTransactionActive && CacheFactoryDirective.SubscribeGlobalDCE.equals(cacheFactoryDirective))
			{
				// This cache is not interested in transactional DCEs
				continue;
			}
			IWritableCache childCache = flcEntry.getFirstLevelCache();
			if (childCache == null)
			{
				// This cache is not valid any more
				continue;
			}
			if (!flcEntry.isInterestedInThread(currentThread))
			{
				// This cache is bound to a different thread than the current one
				continue;
			}
			liveChildCaches.add(childCache);
		}
	}
}
