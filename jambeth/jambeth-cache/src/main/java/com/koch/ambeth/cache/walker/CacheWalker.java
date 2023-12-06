package com.koch.ambeth.cache.walker;

/*-
 * #%L
 * jambeth-cache
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

import com.koch.ambeth.cache.ChildCache;
import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.cache.ioc.CacheModule;
import com.koch.ambeth.cache.proxy.IValueHolderContainer;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.ITransactionState;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheProvider;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.IdentityHashSet;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class CacheWalker implements ICacheWalker {
    private static final IObjRef[] allEntityRefs = new IObjRef[0];
    @Autowired
    protected ICacheProvider cacheProvider;
    @Autowired
    protected ICache firstLevelCache;
    @Autowired
    protected IObjRefHelper objRefHelper;
    @Autowired
    protected IRootCache secondLevelCache;
    @Autowired(CacheModule.COMMITTED_ROOT_CACHE)
    protected IRootCache committedRootCache;
    @Autowired
    protected IMergeProcess mergeProcess;
    @Autowired(optional = true)
    protected ITransactionState transactionState;
    @LogInstance
    private ILogger log;

    @SuppressWarnings("unchecked")
    @Override
    public <T> ICacheWalkerResult walkEntities(T... entities) {
        var objRefs = objRefHelper.extractObjRefList(entities, null);

        var allCachesSet = new IdentityHashSet<ICache>();

        for (var entity : entities) {
            if (entity instanceof IValueHolderContainer) {
                var targetCache = ((IValueHolderContainer) entity).get__TargetCache();
                if (targetCache != null) {
                    allCachesSet.add(targetCache);
                }
            }
        }

        return walkIntern(objRefs.toArray(IObjRef[]::new), allCachesSet);
    }

    @Override
    public ICacheWalkerResult walkAll() {
        return walk(allEntityRefs);
    }

    @Override
    public ICacheWalkerResult walk(IObjRef... objRefs) {
        var allCachesSet = new IdentityHashSet<ICache>();

        return walkIntern(objRefs, allCachesSet);
    }

    protected ICacheWalkerResult walkIntern(IObjRef[] objRefs, IdentityHashSet<ICache> allCachesSet) {
        var cacheToChildCaches = new IdentityHashMap<ICache, List<ICache>>();
        var cacheToProxyCache = new IdentityHashMap<ICache, ICache>();

        var currentCommittedRootCache = committedRootCache.getCurrentCache();

        if (!cacheProvider.isNewInstanceOnCall()) {
            allCachesSet.add(firstLevelCache.getCurrentCache());
        }

        var allChildCaches = allCachesSet.toArray(ICache.class);

        allCachesSet.add(currentCommittedRootCache);
        for (var childCache : allChildCaches) {
            var child = childCache;
            var parent = ((ChildCache) child).getParent();
            while (parent != null) {
                var currentParent = parent.getCurrentCache();

                if (!allCachesSet.add(currentParent)) {
                    // skip this cache. we handled it already
                    break;
                }
                checkParentCache(parent, currentParent, child, cacheToChildCaches, cacheToProxyCache);
                parent = ((IRootCache) currentParent).getParent();
                child = currentParent;
            }
            checkParentCache(committedRootCache, currentCommittedRootCache, child, cacheToChildCaches, cacheToProxyCache);
        }
        if (objRefs != allEntityRefs) {
            objRefs = new HashSet<>(objRefs).toArray(IObjRef[]::new);
            Arrays.sort(objRefs, ObjRef.comparator);
        }
        var rootEntry = buildWalkedEntry(currentCommittedRootCache, objRefs, cacheToChildCaches, cacheToProxyCache);

        if (objRefs == allEntityRefs) {
            var allObjRefs = new HashMap<IObjRef, Integer>();
            collectAllObjRefs(rootEntry, allObjRefs);
            objRefs = !allObjRefs.isEmpty() ? allObjRefs.keyList().toArray(IObjRef[]::new) : ObjRef.EMPTY_ARRAY;
            Arrays.sort(objRefs, ObjRef.comparator);
            for (int a = objRefs.length; a-- > 0; ) {
                allObjRefs.put(objRefs[a], Integer.valueOf(a));
            }
            reallocateObjRefsAndCacheValues(rootEntry, objRefs, allObjRefs);
        }
        return rootEntry;
    }

    protected void reallocateObjRefsAndCacheValues(CacheWalkerResult entry, IObjRef[] objRefs, HashMap<IObjRef, Integer> allObjRefs) {
        var oldObjRefs = entry.objRefs;
        var oldCacheValues = entry.cacheValues;
        var newCacheValues = new Object[objRefs.length];
        for (int oldIndex = oldObjRefs.length; oldIndex-- > 0; ) {
            var oldObjRef = oldObjRefs[oldIndex];
            var newIndex = allObjRefs.get(oldObjRef).intValue();
            newCacheValues[newIndex] = oldCacheValues[oldIndex];
        }
        entry.cacheValues = newCacheValues;
        entry.objRefs = objRefs;
        entry.updatePendingChanges();

        var childEntries = entry.childEntries;
        if (childEntries == null) {
            return;
        }
        if (childEntries.getClass().isArray()) {
            for (var childEntry : (CacheWalkerResult[]) childEntries) {
                reallocateObjRefsAndCacheValues(childEntry, objRefs, allObjRefs);
            }
        } else {
            reallocateObjRefsAndCacheValues((CacheWalkerResult) childEntries, objRefs, allObjRefs);
        }
    }

    protected void collectAllObjRefs(CacheWalkerResult entry, HashMap<IObjRef, Integer> allObjRefs) {
        for (var objRef : entry.objRefs) {
            allObjRefs.putIfNotExists(objRef, null);
        }

        var childEntries = entry.childEntries;
        if (childEntries == null) {
            return;
        }
        if (childEntries.getClass().isArray()) {
            for (var childEntry : (CacheWalkerResult[]) childEntries) {
                collectAllObjRefs(childEntry, allObjRefs);
            }
        } else {
            collectAllObjRefs((CacheWalkerResult) childEntries, allObjRefs);
        }
    }

    protected void checkParentCache(ICache parentCache, ICache currentParentCache, ICache childCache, IdentityHashMap<ICache, List<ICache>> cacheToChildCaches,
            IdentityHashMap<ICache, ICache> cacheToProxyCache) {
        if (currentParentCache != parentCache) {
            cacheToProxyCache.put(currentParentCache, parentCache);
        }
        var childCaches = cacheToChildCaches.get(currentParentCache);
        if (childCaches == null) {
            childCaches = new ArrayList<>();
            cacheToChildCaches.put(currentParentCache, childCaches);
        }
        childCaches.add(childCache);
    }

    protected CacheWalkerResult buildWalkedEntry(ICache cache, IObjRef[] objRefs, IdentityHashMap<ICache, List<ICache>> cacheToChildCaches, IdentityHashMap<ICache, ICache> cacheToProxyCache) {
        var childCaches = cacheToChildCaches.get(cache);
        CacheWalkerResult[] childCacheEntries;

        if (childCaches == null) {
            childCacheEntries = null;
        } else {
            childCacheEntries = new CacheWalkerResult[childCaches.size()];
            for (int a = childCaches.size(); a-- > 0; ) {
                childCacheEntries[a] = buildWalkedEntry(childCaches.get(a), objRefs, cacheToChildCaches, cacheToProxyCache);
            }
        }
        var proxyCache = cacheToProxyCache.get(cache);

        boolean transactional = false, threadLocal = false;
        if (proxyCache != null) {
            threadLocal = true;
            if (transactionState != null && transactionState.isTransactionActive()) {
                transactional = true;
            }
        }

        List<Object> cacheValues;
        if (objRefs != allEntityRefs) {
            if (cache instanceof ChildCache) {
                cacheValues = cache.getObjects(objRefs, EnumSet.of(CacheDirective.FailEarly, CacheDirective.ReturnMisses));
            } else {
                cacheValues = cache.getObjects(objRefs, EnumSet.of(CacheDirective.FailEarly, CacheDirective.CacheValueResult, CacheDirective.ReturnMisses));
            }
        } else {
            var fCacheValues = new IdentityHashSet<>();
            cache.getContent((entityType, idIndex, id, value) -> fCacheValues.add(value));
            cacheValues = fCacheValues.toList();
            var objRefList = new ArrayList<IObjRef>(cacheValues.size());

            // generate ad-hoc objRefs
            objRefs = !cacheValues.isEmpty() ? objRefHelper.extractObjRefList(cacheValues, null, objRefList).toArray(IObjRef[]::new) : ObjRef.EMPTY_ARRAY;
        }
        Object childEntries = childCacheEntries;
        if (childCacheEntries != null && childCacheEntries.length == 1) {
            childEntries = childCacheEntries[0];
        }
        var parentEntry = new CacheWalkerResult(cache, transactional, threadLocal, objRefs, cacheValues.toArray(), childEntries);
        if (childCacheEntries != null) {
            for (int a = childCacheEntries.length; a-- > 0; ) {
                childCacheEntries[a].setParentEntry(parentEntry);
            }
        }
        if (objRefs != allEntityRefs) {
            parentEntry.updatePendingChanges();
        }
        return parentEntry;
    }
}
