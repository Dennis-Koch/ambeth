using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Walker
{
    public class CacheWalker : ICacheWalker
    {
        private static readonly IObjRef[] allEntityRefs = new IObjRef[0];

        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public ICacheProvider CacheProvider { protected get; set; }

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
                    ICache targetCache = ((IValueHolderContainer)entity).__TargetCache;
                    if (targetCache != null)
                    {
                        allCachesSet.Add(targetCache);
                    }
                }
            }

            return WalkIntern(ListUtil.ToArray(objRefs), allCachesSet);
        }

        public ICacheWalkerResult WalkAll()
        {
            return Walk(allEntityRefs);
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

            if (!CacheProvider.IsNewInstanceOnCall)
            {
                allCachesSet.Add(FirstLevelCache.CurrentCache);
            }

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
            if (objRefs != allEntityRefs)
		    {
			    objRefs = new CHashSet<IObjRef>(objRefs).ToArray();
			    Array.Sort(objRefs, ObjRef.comparator);
		    }
		    CacheWalkerResult rootEntry = BuildWalkedEntry(currentCommittedRootCache, objRefs, cacheToChildCaches, cacheToProxyCache);

		    if (objRefs == allEntityRefs)
		    {
			    HashMap<IObjRef, int?> allObjRefs = new HashMap<IObjRef, int?>();
			    CollectAllObjRefs(rootEntry, allObjRefs);
			    objRefs = allObjRefs.Count > 0 ? ListUtil.ToArray(allObjRefs.KeyList()) : ObjRef.EMPTY_ARRAY;
			    Array.Sort(objRefs, ObjRef.comparator);
			    for (int a = objRefs.Length; a-- > 0;)
			    {
				    allObjRefs.Put(objRefs[a], a);
			    }
			    ReallocateObjRefsAndCacheValues(rootEntry, objRefs, allObjRefs);
		    }
		    return rootEntry;
        }

        protected void ReallocateObjRefsAndCacheValues(CacheWalkerResult entry, IObjRef[] objRefs, HashMap<IObjRef, int?> allObjRefs)
	    {
		    IObjRef[] oldObjRefs = entry.objRefs;
		    Object[] oldCacheValues = entry.cacheValues;
		    Object[] newCacheValues = new Object[objRefs.Length];
            for (int oldIndex = oldObjRefs.Length; oldIndex-- > 0; )
		    {
			    IObjRef oldObjRef = oldObjRefs[oldIndex];
			    int? newIndex = allObjRefs.Get(oldObjRef);
			    newCacheValues[newIndex.Value] = oldCacheValues[oldIndex];
		    }
		    entry.cacheValues = newCacheValues;
		    entry.objRefs = objRefs;
		    entry.UpdatePendingChanges();

		    Object childEntries = entry.childEntries;
		    if (childEntries == null)
		    {
			    return;
		    }
            if (childEntries.GetType().IsArray)
		    {
			    foreach (CacheWalkerResult childEntry in (CacheWalkerResult[]) childEntries)
			    {
				    ReallocateObjRefsAndCacheValues(childEntry, objRefs, allObjRefs);
			    }
		    }
		    else
		    {
			    ReallocateObjRefsAndCacheValues((CacheWalkerResult) childEntries, objRefs, allObjRefs);
		    }
	    }

        protected void CollectAllObjRefs(CacheWalkerResult entry, HashMap<IObjRef, int?> allObjRefs)
	    {
		    foreach (IObjRef objRef in entry.objRefs)
		    {
			    allObjRefs.PutIfNotExists(objRef, null);
		    }

		    Object childEntries = entry.childEntries;
		    if (childEntries == null)
		    {
			    return;
		    }
		    if (childEntries.GetType().IsArray)
		    {
			    foreach (CacheWalkerResult childEntry in (CacheWalkerResult[]) childEntries)
			    {
				    CollectAllObjRefs(childEntry, allObjRefs);
			    }
		    }
		    else
		    {
			    CollectAllObjRefs((CacheWalkerResult) childEntries, allObjRefs);
		    }
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

            IList<Object> cacheValues;
            if (objRefs != allEntityRefs)
            {
                if (cache is ChildCache)
                {
                    cacheValues = cache.GetObjects(objRefs, CacheDirective.FailEarly | CacheDirective.ReturnMisses);
                }
                else
                {
                    cacheValues = cache.GetObjects(objRefs, CacheDirective.FailEarly | CacheDirective.CacheValueResult | CacheDirective.ReturnMisses);
                }
            }
            else
            {
                IdentityHashSet<Object> fCacheValues = new IdentityHashSet<Object>();
			    cache.GetContent(new HandleContentDelegate(delegate(Type entityType, sbyte idIndex, Object id, Object value)
				    {
					    fCacheValues.Add(value);
				    }));
			    cacheValues = fCacheValues.ToList();

			    // generate ad-hoc objRefs
			    objRefs = cacheValues.Count > 0 ? ListUtil.ToArray(ObjRefHelper.ExtractObjRefList(cacheValues, null)) : ObjRef.EMPTY_ARRAY;
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
            if (objRefs != allEntityRefs)
		    {
			    parentEntry.UpdatePendingChanges();
		    }
            return parentEntry;
        }
    }
}