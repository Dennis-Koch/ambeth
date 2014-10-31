using De.Osthus.Ambeth.Ioc;
using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Util;
using System.Threading;

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
                if (Log.DebugEnabled)
                {
                    String foreignThreadText;
                    if (foreignThreadAware)
                    {
                        foreignThreadText = ", multithreaded";
                    }
                    else
                    {
                        foreignThreadText = ", to thread " + thread.ManagedThreadId + ":" + thread.Name;
                    }
                    String privilegedText;
                    if (firstLevelCache.Privileged)
                    {
                        privilegedText = ", privileged";
                    }
                    else
                    {
                        privilegedText = ", non-privileged";
                    }
                    if (CacheFactoryDirective.SubscribeTransactionalDCE.Equals(flcEntry.GetCacheFactoryDirective()))
                    {
                        Log.Debug("Registered FLC with id: " + firstLevelCache.CacheId + privilegedText + ", transactional" + foreignThreadText);
                    }
                    else if (CacheFactoryDirective.SubscribeGlobalDCE.Equals(flcEntry.GetCacheFactoryDirective()))
                    {
                        Log.Debug("Registered FLC with id: " + firstLevelCache.CacheId + privilegedText + ", non-transactional" + foreignThreadText);
                    }
                    else
                    {
                        Log.Debug("Registered FLC with id: " + firstLevelCache.CacheId + privilegedText + ", traced");
                    }
                }
            }
            finally
            {
                unboundWriteLock.Unlock();
            }
        }

        public void UnregisterFirstLevelCache(IWritableCache firstLevelCache, CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware)
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
                if (Log.DebugEnabled)
                {
                    String foreignThreadText;
                    if (foreignThreadAware)
                    {
                        foreignThreadText = ", multithreaded";
                    }
                    else
                    {
                        Thread thread = flcEntry.GetOwningThread();
                        if (thread == null)
                        {
                            foreignThreadText = ", from unknown thread";
                        }
                        else
                        {
                            foreignThreadText = ", from thread " + thread.ManagedThreadId + ":" + thread.Name;
                        }
                    }
                    if (CacheFactoryDirective.SubscribeTransactionalDCE.Equals(flcEntry.GetCacheFactoryDirective()))
                    {
                        Log.Debug("Unregistered FLC with id: " + firstLevelCache.CacheId + ", transactional" + foreignThreadText);
                    }
                    else if (CacheFactoryDirective.SubscribeGlobalDCE.Equals(flcEntry.GetCacheFactoryDirective()))
                    {
                        Log.Debug("Unregistered FLC with id: " + firstLevelCache.CacheId + ", non-transactional" + foreignThreadText);
                    }
                    else
                    {
                        Log.Debug("Unregistered FLC with id: " + firstLevelCache.CacheId + " traced");
                    }
                }
                firstLevelCache.CacheId = 0;
            }
            finally
            {
                unboundWriteLock.Unlock();
            }
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