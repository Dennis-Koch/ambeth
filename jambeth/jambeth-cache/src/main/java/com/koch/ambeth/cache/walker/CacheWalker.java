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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

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
import com.koch.ambeth.merge.cache.HandleContentDelegate;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheProvider;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.IdentityHashSet;

public class CacheWalker implements ICacheWalker {
	private static final IObjRef[] allEntityRefs = new IObjRef[0];

	@LogInstance
	private ILogger log;

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

	@Override
	public <T> ICacheWalkerResult walkEntities(T... entities) {
		IList<IObjRef> objRefs = new ArrayList<>(entities.length);
		objRefs = objRefHelper.extractObjRefList(entities, null, objRefs);

		IdentityHashSet<ICache> allCachesSet = new IdentityHashSet<>();

		for (Object entity : entities) {
			if (entity instanceof IValueHolderContainer) {
				ICache targetCache = ((IValueHolderContainer) entity).get__TargetCache();
				if (targetCache != null) {
					allCachesSet.add(targetCache);
				}
			}
		}

		return walkIntern(objRefs.toArray(IObjRef.class), allCachesSet);
	}

	@Override
	public ICacheWalkerResult walkAll() {
		return walk(allEntityRefs);
	}

	@Override
	public ICacheWalkerResult walk(IObjRef... objRefs) {
		IdentityHashSet<ICache> allCachesSet = new IdentityHashSet<>();

		return walkIntern(objRefs, allCachesSet);
	}

	protected ICacheWalkerResult walkIntern(IObjRef[] objRefs, IdentityHashSet<ICache> allCachesSet) {
		IdentityHashMap<ICache, List<ICache>> cacheToChildCaches =
				new IdentityHashMap<>();
		IdentityHashMap<ICache, ICache> cacheToProxyCache = new IdentityHashMap<>();

		ICache currentCommittedRootCache = committedRootCache.getCurrentCache();

		if (!cacheProvider.isNewInstanceOnCall()) {
			allCachesSet.add(firstLevelCache.getCurrentCache());
		}

		ICache[] allChildCaches = allCachesSet.toArray(ICache.class);

		allCachesSet.add(currentCommittedRootCache);
		for (ICache childCache : allChildCaches) {
			ICache child = childCache;
			ICache parent = ((ChildCache) child).getParent();
			while (parent != null) {
				ICache currentParent = parent.getCurrentCache();

				if (!allCachesSet.add(currentParent)) {
					// skip this cache. we handled it already
					break;
				}
				checkParentCache(parent, currentParent, child, cacheToChildCaches, cacheToProxyCache);
				parent = ((IRootCache) currentParent).getParent();
				child = currentParent;
			}
			checkParentCache(committedRootCache, currentCommittedRootCache, child, cacheToChildCaches,
					cacheToProxyCache);
		}
		if (objRefs != allEntityRefs) {
			objRefs = new HashSet<>(objRefs).toArray(IObjRef.class);
			Arrays.sort(objRefs, ObjRef.comparator);
		}
		CacheWalkerResult rootEntry =
				buildWalkedEntry(currentCommittedRootCache, objRefs, cacheToChildCaches, cacheToProxyCache);

		if (objRefs == allEntityRefs) {
			HashMap<IObjRef, Integer> allObjRefs = new HashMap<>();
			collectAllObjRefs(rootEntry, allObjRefs);
			objRefs =
					allObjRefs.size() > 0 ? allObjRefs.keyList().toArray(IObjRef.class) : ObjRef.EMPTY_ARRAY;
			Arrays.sort(objRefs, ObjRef.comparator);
			for (int a = objRefs.length; a-- > 0;) {
				allObjRefs.put(objRefs[a], Integer.valueOf(a));
			}
			reallocateObjRefsAndCacheValues(rootEntry, objRefs, allObjRefs);
		}
		return rootEntry;
	}

	protected void reallocateObjRefsAndCacheValues(CacheWalkerResult entry, IObjRef[] objRefs,
			HashMap<IObjRef, Integer> allObjRefs) {
		IObjRef[] oldObjRefs = entry.objRefs;
		Object[] oldCacheValues = entry.cacheValues;
		Object[] newCacheValues = new Object[objRefs.length];
		for (int oldIndex = oldObjRefs.length; oldIndex-- > 0;) {
			IObjRef oldObjRef = oldObjRefs[oldIndex];
			int newIndex = allObjRefs.get(oldObjRef).intValue();
			newCacheValues[newIndex] = oldCacheValues[oldIndex];
		}
		entry.cacheValues = newCacheValues;
		entry.objRefs = objRefs;
		entry.updatePendingChanges();

		Object childEntries = entry.childEntries;
		if (childEntries == null) {
			return;
		}
		if (childEntries.getClass().isArray()) {
			for (CacheWalkerResult childEntry : (CacheWalkerResult[]) childEntries) {
				reallocateObjRefsAndCacheValues(childEntry, objRefs, allObjRefs);
			}
		}
		else {
			reallocateObjRefsAndCacheValues((CacheWalkerResult) childEntries, objRefs, allObjRefs);
		}
	}

	protected void collectAllObjRefs(CacheWalkerResult entry, HashMap<IObjRef, Integer> allObjRefs) {
		for (IObjRef objRef : entry.objRefs) {
			allObjRefs.putIfNotExists(objRef, null);
		}

		Object childEntries = entry.childEntries;
		if (childEntries == null) {
			return;
		}
		if (childEntries.getClass().isArray()) {
			for (CacheWalkerResult childEntry : (CacheWalkerResult[]) childEntries) {
				collectAllObjRefs(childEntry, allObjRefs);
			}
		}
		else {
			collectAllObjRefs((CacheWalkerResult) childEntries, allObjRefs);
		}
	}

	protected void checkParentCache(ICache parentCache, ICache currentParentCache, ICache childCache,
			IdentityHashMap<ICache, List<ICache>> cacheToChildCaches,
			IdentityHashMap<ICache, ICache> cacheToProxyCache) {
		if (currentParentCache != parentCache) {
			cacheToProxyCache.put(currentParentCache, parentCache);
		}
		List<ICache> childCaches = cacheToChildCaches.get(currentParentCache);
		if (childCaches == null) {
			childCaches = new ArrayList<>();
			cacheToChildCaches.put(currentParentCache, childCaches);
		}
		childCaches.add(childCache);
	}

	protected CacheWalkerResult buildWalkedEntry(ICache cache, IObjRef[] objRefs,
			IdentityHashMap<ICache, List<ICache>> cacheToChildCaches,
			IdentityHashMap<ICache, ICache> cacheToProxyCache) {
		List<ICache> childCaches = cacheToChildCaches.get(cache);
		CacheWalkerResult[] childCacheEntries;

		if (childCaches == null) {
			childCacheEntries = null;
		}
		else {
			childCacheEntries = new CacheWalkerResult[childCaches.size()];
			for (int a = childCaches.size(); a-- > 0;) {
				childCacheEntries[a] =
						buildWalkedEntry(childCaches.get(a), objRefs, cacheToChildCaches, cacheToProxyCache);
			}
		}
		ICache proxyCache = cacheToProxyCache.get(cache);

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
				cacheValues = cache.getObjects(objRefs,
						EnumSet.of(CacheDirective.FailEarly, CacheDirective.ReturnMisses));
			}
			else {
				cacheValues = cache.getObjects(objRefs, EnumSet.of(CacheDirective.FailEarly,
						CacheDirective.CacheValueResult, CacheDirective.ReturnMisses));
			}
		}
		else {
			final IdentityHashSet<Object> fCacheValues = new IdentityHashSet<>();
			cache.getContent(new HandleContentDelegate() {
				@Override
				public void invoke(Class<?> entityType, byte idIndex, Object id, Object value) {
					fCacheValues.add(value);
				}
			});
			cacheValues = fCacheValues.toList();

			// generate ad-hoc objRefs
			objRefs = cacheValues.size() > 0
					? objRefHelper.extractObjRefList(cacheValues, null).toArray(IObjRef.class)
					: ObjRef.EMPTY_ARRAY;
		}
		Object childEntries = childCacheEntries;
		if (childCacheEntries != null && childCacheEntries.length == 1) {
			childEntries = childCacheEntries[0];
		}
		CacheWalkerResult parentEntry = new CacheWalkerResult(cache, transactional, threadLocal,
				objRefs, cacheValues.toArray(), childEntries);
		if (childCacheEntries != null) {
			for (int a = childCacheEntries.length; a-- > 0;) {
				childCacheEntries[a].setParentEntry(parentEntry);
			}
		}
		if (objRefs != allEntityRefs) {
			parentEntry.updatePendingChanges();
		}
		return parentEntry;
	}
}
