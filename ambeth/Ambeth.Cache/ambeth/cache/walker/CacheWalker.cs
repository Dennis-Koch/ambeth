using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Walker
{
    public class CacheWalker : ICacheWalker
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public ICache FirstLevelCache { protected get; set; }

        [Autowired]
        public IObjRefHelper ObjRefHelper { protected get; set; }

        [Autowired]
        public IRootCache SecondLevelCache { protected get; set; }

        [Autowired(CacheModule.COMMITTED_ROOT_CACHE)]
        public IRootCache CommittedRootCache { protected get; set; }

        [Autowired]
        public IMergeProcess MergeProcess { protected get; set; }

        [Autowired(Optional = true)]
        public ITransactionState TransactionState { protected get; set; }

        public ICacheWalkerResult WalkEntities<T>(params T[] entities)
        {
            IList<IObjRef> objRefs = new List<IObjRef>(entities.Length);
            objRefs = ObjRefHelper.ExtractObjRefList(entities, null, objRefs);

            IdentityHashSet<ICache> allCachesSet = new IdentityHashSet<ICache>();

            foreach (Object entity in entities)
            {
                if (entity is IValueHolderContainer)
                {
                    ICacheIntern targetCache = ((IValueHolderContainer)entity).__TargetCache;
                    if (targetCache != null)
                    {
                        allCachesSet.Add(targetCache);
                    }
                }
            }

            return WalkIntern(ListUtil.ToArray(objRefs), allCachesSet);
        }

        public ICacheWalkerResult Walk(params IObjRef[] objRefs)
        {
            IdentityHashSet<ICache> allCachesSet = new IdentityHashSet<ICache>();

            return WalkIntern(objRefs, allCachesSet);
        }

        protected ICacheWalkerResult WalkIntern(IObjRef[] objRefs, IdentityHashSet<ICache> allCachesSet)
        {
            IdentityHashMap<ICache, List<ICache>> cacheToChildCaches = new IdentityHashMap<ICache, List<ICache>>();
            IdentityHashMap<ICache, ICache> cacheToProxyCache = new IdentityHashMap<ICache, ICache>();

            ICache currentCommittedRootCache = CommittedRootCache.CurrentCache;

            allCachesSet.Add(this.FirstLevelCache.CurrentCache);

            ICache[] allChildCaches = allCachesSet.ToArray();

            allCachesSet.Add(currentCommittedRootCache);
            foreach (ICache childCache in allChildCaches)
            {
                ICache child = childCache;
                ICache parent = ((ChildCache)child).Parent;
                while (parent != null)
                {
                    ICache currentParent = parent.CurrentCache;

                    if (!allCachesSet.Add(currentParent))
                    {
                        // skip this cache. we handled it already
                        break;
                    }
                    CheckParentCache(parent, currentParent, child, cacheToChildCaches, cacheToProxyCache);
                    parent = ((RootCache)currentParent).Parent;
                    child = currentParent;
                }
                CheckParentCache(CommittedRootCache, currentCommittedRootCache, child, cacheToChildCaches, cacheToProxyCache);
            }
            CHashSet<IObjRef> objRefsSet = new CHashSet<IObjRef>(objRefs);
            IObjRef[] objRefsArray = objRefsSet.ToArray();

            Array.Sort(objRefsArray, new Comparison<IObjRef>(delegate(IObjRef o1, IObjRef o2)
            {
                int result = o1.RealType.FullName.CompareTo(o2.RealType.FullName);
                if (result != 0)
                {
                    return result;
                }
                result = o1.IdNameIndex == o2.IdNameIndex ? 0 : o1.IdNameIndex > o2.IdNameIndex ? 1 : -1;
                if (result != 0)
                {
                    return result;
                }
                return o1.Id.ToString().CompareTo(o2.Id.ToString());
            }));
            return BuildWalkedEntry(currentCommittedRootCache, objRefsArray, cacheToChildCaches, cacheToProxyCache);
        }

        protected void CheckParentCache(ICache parentCache, ICache currentParentCache, ICache childCache, IdentityHashMap<ICache, List<ICache>> cacheToChildCaches,
                IdentityHashMap<ICache, ICache> cacheToProxyCache)
        {
            if (currentParentCache != parentCache)
            {
                cacheToProxyCache.Put(currentParentCache, parentCache);
            }
            List<ICache> childCaches = cacheToChildCaches.Get(currentParentCache);
            if (childCaches == null)
            {
                childCaches = new List<ICache>();
                cacheToChildCaches.Put(currentParentCache, childCaches);
            }
            childCaches.Add(childCache);
        }

        protected CacheWalkerResult BuildWalkedEntry(ICache cache, IObjRef[] objRefs, IdentityHashMap<ICache, List<ICache>> cacheToChildCaches,
                IdentityHashMap<ICache, ICache> cacheToProxyCache)
        {
            List<ICache> childCaches = cacheToChildCaches.Get(cache);
            CacheWalkerResult[] childCacheEntries;

            if (childCaches == null)
            {
                childCacheEntries = null;
            }
            else
            {
                childCacheEntries = new CacheWalkerResult[childCaches.Count];
                for (int a = childCaches.Count; a-- > 0; )
                {
                    childCacheEntries[a] = BuildWalkedEntry(childCaches[a], objRefs, cacheToChildCaches, cacheToProxyCache);
                }
            }
            ICache proxyCache = cacheToProxyCache.Get(cache);

            bool transactional = false, threadLocal = false;
            if (proxyCache != null)
            {
                threadLocal = true;
                if (TransactionState != null && TransactionState.IsTransactionActive)
                {
                    transactional = true;
                }
            }

            IList<Object> cacheValues = null;
            if (cache is ChildCache)
            {
                cacheValues = cache.GetObjects(objRefs, CacheDirective.FailEarly | CacheDirective.ReturnMisses);
            }
            else
            {
                cacheValues = cache.GetObjects(objRefs, CacheDirective.FailEarly | CacheDirective.CacheValueResult | CacheDirective.ReturnMisses);
            }
            Object childEntries = childCacheEntries;
            if (childCacheEntries != null && childCacheEntries.Length == 1)
            {
                childEntries = childCacheEntries[0];
            }
            CacheWalkerResult parentEntry = new CacheWalkerResult(cache, transactional, threadLocal, objRefs, ListUtil.ToArray(cacheValues), childEntries);
            if (childCacheEntries != null)
            {
                for (int a = childCacheEntries.Length; a-- > 0; )
                {
                    childCacheEntries[a].ParentEntry = parentEntry;
                }
            }
            return parentEntry;
        }
    }
}