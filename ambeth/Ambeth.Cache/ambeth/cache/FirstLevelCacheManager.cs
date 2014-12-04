using De.Osthus.Ambeth.Ioc;
using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Util;
using System.Threading;
using System.Text;

namespace De.Osthus.Ambeth.Cache
{
    public class FirstLevelCacheManager : IInitializingBean, IFirstLevelCacheExtendable, IFirstLevelCacheManager
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        protected readonly IDictionary<int, FlcEntry> allFLCs = new Dictionary<int, FlcEntry>();

        protected int changeCount, lastCacheId;

        public ITransactionState TransactionState { protected get; set; }

        protected readonly Lock unboundReadLock, unboundWriteLock;

        public FirstLevelCacheManager()
        {
            ReadWriteLock rwLock = new ReadWriteLock();
            unboundReadLock = rwLock.ReadLock;
            unboundWriteLock = rwLock.WriteLock;
        }

        public void AfterPropertiesSet()
        {
            // Intended blank
        }

        protected WeakReference CreateReferenceEntry<V>(V firstLevelCache)
        {
            return new WeakReference(firstLevelCache);
        }

        protected void CleanupCaches()
        {
            DictionaryExtension.LoopModifiable(allFLCs, delegate(int cacheId, FlcEntry flcEntry)
            {
                IWritableCache writableCache = flcEntry.GetFirstLevelCache();
                if (writableCache != null)
                {
                    return;
                }
                allFLCs.Remove(cacheId);
                if (Log.DebugEnabled)
                {
                    Log.Debug("GCed first level cache with id: " + cacheId);
                }
            });
        }

        protected void CheckCachesForCleanup()
        {
            changeCount++;
            if (changeCount >= 1000)
            {
                CleanupCaches();
                changeCount = 0;
            }
        }

        public void RegisterFirstLevelCache(IWritableCache firstLevelCache, CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware)
        {
            RegisterFirstLevelCache(firstLevelCache, cacheFactoryDirective, foreignThreadAware, null);
        }

        public void RegisterFirstLevelCache(IWritableCache firstLevelCache, CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware, String name)
        {
            WeakReference firstLevelCacheR = CreateReferenceEntry(firstLevelCache);
            Thread thread = Thread.CurrentThread;
            WeakReference owningThreadR = !foreignThreadAware ? CreateReferenceEntry(thread) : null;
            FlcEntry flcEntry = new FlcEntry(cacheFactoryDirective, firstLevelCacheR, owningThreadR);
            unboundWriteLock.Lock();
            try
            {
                int cacheId;
                int nextCacheId = lastCacheId;
                while (true)
                {
                    if (nextCacheId == Int32.MaxValue)
                    {
                        nextCacheId = 0;
                    }
                    cacheId = ++nextCacheId;

                    if (!allFLCs.ContainsKey(cacheId))
                    {
                        allFLCs.Add(cacheId, flcEntry);
                        lastCacheId = cacheId;
                        break;
                    }
                }
                firstLevelCache.CacheId = cacheId;

                LogFLC(firstLevelCache, cacheFactoryDirective, foreignThreadAware, name, flcEntry, true);
            }
            finally
            {
                unboundWriteLock.Unlock();
            }
        }

        public void UnregisterFirstLevelCache(IWritableCache firstLevelCache, CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware)
        {
            UnregisterFirstLevelCache(firstLevelCache, cacheFactoryDirective, foreignThreadAware, null);
        }

        public void UnregisterFirstLevelCache(IWritableCache firstLevelCache, CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware, String name)
        {
            // cacheFactoryDirective and foreignThreadAware will be intentionally ignored at unregister

            unboundWriteLock.Lock();
            try
            {
                CheckCachesForCleanup();
                int cacheId = firstLevelCache.CacheId;
                FlcEntry flcEntry = DictionaryExtension.ValueOrDefault(allFLCs, cacheId);
                if (flcEntry == null)
                {
                    throw new Exception("CacheId is not mapped to a valid cache instance");
                }
                IWritableCache existingChildCache = flcEntry.GetFirstLevelCache();
                if (existingChildCache == null)
                {
                    throw new Exception("Fatal error occured. Reference lost to cache");
                }
                if (existingChildCache != firstLevelCache)
                {
                    throw new Exception("Fatal error occured. CacheId invalid - it is not mapped to the specified cache instance");
                }
                allFLCs.Remove(cacheId);
                foreignThreadAware = flcEntry.IsForeignThreadAware();
                cacheFactoryDirective = flcEntry.GetCacheFactoryDirective();

                LogFLC(firstLevelCache, cacheFactoryDirective, foreignThreadAware, name, flcEntry, false);
                firstLevelCache.CacheId = 0;
            }
            finally
            {
                unboundWriteLock.Unlock();
            }
        }

        protected void LogFLC(IWritableCache firstLevelCache, CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware, String name,
			FlcEntry flcEntry, bool isRegister)
	    {
		    if (!Log.DebugEnabled)
		    {
			    return;
		    }
		    StringBuilder sb = new StringBuilder();
			if (isRegister)
			{
				sb.Append("Registered");
			}
			else
			{
				sb.Append("Unregistered");
			}
			sb.Append(" FLC");
			if (name != null)
			{
				sb.Append(" '").Append(name).Append("'");
			}
			sb.Append(" with id: ").Append(firstLevelCache.CacheId);
			if (firstLevelCache.Privileged)
			{
				sb.Append(", privileged");
			}
			else
			{
				sb.Append(", non-privileged");
			}
			if (CacheFactoryDirective.SubscribeTransactionalDCE.Equals(flcEntry.GetCacheFactoryDirective()))
			{
				sb.Append(", transactional");
				if (foreignThreadAware)
				{
					sb.Append(", multithreaded");
				}
				else
				{
                    Thread thread = flcEntry.GetOwningThread();
                    sb.Append(", to thread ").Append(thread.ManagedThreadId).Append(':').Append(thread.Name);
				}
			}
            else if (CacheFactoryDirective.SubscribeGlobalDCE.Equals(flcEntry.GetCacheFactoryDirective()))
			{
				sb.Append(", non-transactional");
				if (foreignThreadAware)
				{
					sb.Append(", multithreaded");
				}
				else
				{
					Thread thread = flcEntry.GetOwningThread();
                    sb.Append(", to thread ").Append(thread.ManagedThreadId).Append(':').Append(thread.Name);
				}
			}
			else
			{
				sb.Append(", traced");
			}
			Log.Debug(sb.ToString());
	    }

        public IList<IWritableCache> SelectFirstLevelCaches()
        {
            bool isTransactionActive = false;
            if (TransactionState != null)
            {
                isTransactionActive = TransactionState.IsTransactionActive;
            }
            IList<IWritableCache> liveChildCaches = new List<IWritableCache>();

            unboundReadLock.Lock();
            try
            {
                AddLiveFirstLevelCaches(liveChildCaches, isTransactionActive);
            }
            finally
            {
                unboundReadLock.Unlock();
            }
            return liveChildCaches;
        }

        protected void AddLiveFirstLevelCaches(IList<IWritableCache> liveChildCaches, bool isTransactionActive)
        {
            if (allFLCs.Count == 0)
            {
                return;
            }
            Thread currentThread = Thread.CurrentThread;
            DictionaryExtension.Loop(allFLCs, delegate(int cacheId, FlcEntry flcEntry)
            {
                CacheFactoryDirective cacheFactoryDirective = flcEntry.GetCacheFactoryDirective();
                if (CacheFactoryDirective.NoDCE.Equals(cacheFactoryDirective))
                {
                    // This cache is not interested in DCEs at all
                    return;
                }
                if (isTransactionActive && CacheFactoryDirective.SubscribeGlobalDCE.Equals(cacheFactoryDirective))
                {
                    // This cache is not interested in transactional DCEs
                    return;
                }
                IWritableCache childCache = flcEntry.GetFirstLevelCache();
                if (childCache == null)
                {
                    // This cache is not valid any more
                    return;
                }
                if (!flcEntry.IsInterestedInThread(currentThread))
                {
                    // This cache is bound to a different thread than the current one
                    return;
                }
                liveChildCaches.Add(childCache);
            });
        }
    }
}