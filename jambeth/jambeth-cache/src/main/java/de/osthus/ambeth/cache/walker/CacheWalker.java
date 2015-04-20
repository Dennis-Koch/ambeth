package de.osthus.ambeth.cache.walker;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.ChildCache;
import de.osthus.ambeth.cache.HandleContentDelegate;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheProvider;
import de.osthus.ambeth.cache.IRootCache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.ioc.CacheModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.ITransactionState;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.proxy.IValueHolderContainer;

public class CacheWalker implements ICacheWalker
{
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
	public <T> ICacheWalkerResult walkEntities(T... entities)
	{
		IList<IObjRef> objRefs = new ArrayList<IObjRef>(entities.length);
		objRefs = objRefHelper.extractObjRefList(entities, null, objRefs);

		IdentityHashSet<ICache> allCachesSet = new IdentityHashSet<ICache>();

		for (Object entity : entities)
		{
			if (entity instanceof IValueHolderContainer)
			{
				ICache targetCache = ((IValueHolderContainer) entity).get__TargetCache();
				if (targetCache != null)
				{
					allCachesSet.add(targetCache);
				}
			}
		}

		return walkIntern(objRefs.toArray(IObjRef.class), allCachesSet);
	}

	@Override
	public ICacheWalkerResult walkAll()
	{
		return walk(allEntityRefs);
	}

	@Override
	public ICacheWalkerResult walk(IObjRef... objRefs)
	{
		IdentityHashSet<ICache> allCachesSet = new IdentityHashSet<ICache>();

		return walkIntern(objRefs, allCachesSet);
	}

	protected ICacheWalkerResult walkIntern(IObjRef[] objRefs, IdentityHashSet<ICache> allCachesSet)
	{
		IdentityHashMap<ICache, List<ICache>> cacheToChildCaches = new IdentityHashMap<ICache, List<ICache>>();
		IdentityHashMap<ICache, ICache> cacheToProxyCache = new IdentityHashMap<ICache, ICache>();

		ICache currentCommittedRootCache = committedRootCache.getCurrentCache();

		if (!cacheProvider.isNewInstanceOnCall())
		{
			allCachesSet.add(firstLevelCache.getCurrentCache());
		}

		ICache[] allChildCaches = allCachesSet.toArray(ICache.class);

		allCachesSet.add(currentCommittedRootCache);
		for (ICache childCache : allChildCaches)
		{
			ICache child = childCache;
			ICache parent = ((ChildCache) child).getParent();
			while (parent != null)
			{
				ICache currentParent = parent.getCurrentCache();

				if (!allCachesSet.add(currentParent))
				{
					// skip this cache. we handled it already
					break;
				}
				checkParentCache(parent, currentParent, child, cacheToChildCaches, cacheToProxyCache);
				parent = ((IRootCache) currentParent).getParent();
				child = currentParent;
			}
			checkParentCache(committedRootCache, currentCommittedRootCache, child, cacheToChildCaches, cacheToProxyCache);
		}
		if (objRefs != allEntityRefs)
		{
			objRefs = new HashSet<IObjRef>(objRefs).toArray(IObjRef.class);
			Arrays.sort(objRefs, ObjRef.comparator);
		}
		CacheWalkerResult rootEntry = buildWalkedEntry(currentCommittedRootCache, objRefs, cacheToChildCaches, cacheToProxyCache);

		if (objRefs == allEntityRefs)
		{
			HashMap<IObjRef, Integer> allObjRefs = new HashMap<IObjRef, Integer>();
			collectAllObjRefs(rootEntry, allObjRefs);
			objRefs = allObjRefs.size() > 0 ? allObjRefs.keyList().toArray(IObjRef.class) : ObjRef.EMPTY_ARRAY;
			Arrays.sort(objRefs, ObjRef.comparator);
			for (int a = objRefs.length; a-- > 0;)
			{
				allObjRefs.put(objRefs[a], Integer.valueOf(a));
			}
			reallocateObjRefsAndCacheValues(rootEntry, objRefs, allObjRefs);
		}
		return rootEntry;
	}

	protected void reallocateObjRefsAndCacheValues(CacheWalkerResult entry, IObjRef[] objRefs, HashMap<IObjRef, Integer> allObjRefs)
	{
		IObjRef[] oldObjRefs = entry.objRefs;
		Object[] oldCacheValues = entry.cacheValues;
		Object[] newCacheValues = new Object[objRefs.length];
		for (int oldIndex = oldObjRefs.length; oldIndex-- > 0;)
		{
			IObjRef oldObjRef = oldObjRefs[oldIndex];
			int newIndex = allObjRefs.get(oldObjRef).intValue();
			newCacheValues[newIndex] = oldCacheValues[oldIndex];
		}
		entry.cacheValues = newCacheValues;
		entry.objRefs = objRefs;
		entry.updatePendingChanges();

		Object childEntries = entry.childEntries;
		if (childEntries == null)
		{
			return;
		}
		if (childEntries.getClass().isArray())
		{
			for (CacheWalkerResult childEntry : (CacheWalkerResult[]) childEntries)
			{
				reallocateObjRefsAndCacheValues(childEntry, objRefs, allObjRefs);
			}
		}
		else
		{
			reallocateObjRefsAndCacheValues((CacheWalkerResult) childEntries, objRefs, allObjRefs);
		}
	}

	protected void collectAllObjRefs(CacheWalkerResult entry, HashMap<IObjRef, Integer> allObjRefs)
	{
		for (IObjRef objRef : entry.objRefs)
		{
			allObjRefs.putIfNotExists(objRef, null);
		}

		Object childEntries = entry.childEntries;
		if (childEntries == null)
		{
			return;
		}
		if (childEntries.getClass().isArray())
		{
			for (CacheWalkerResult childEntry : (CacheWalkerResult[]) childEntries)
			{
				collectAllObjRefs(childEntry, allObjRefs);
			}
		}
		else
		{
			collectAllObjRefs((CacheWalkerResult) childEntries, allObjRefs);
		}
	}

	protected void checkParentCache(ICache parentCache, ICache currentParentCache, ICache childCache, IdentityHashMap<ICache, List<ICache>> cacheToChildCaches,
			IdentityHashMap<ICache, ICache> cacheToProxyCache)
	{
		if (currentParentCache != parentCache)
		{
			cacheToProxyCache.put(currentParentCache, parentCache);
		}
		List<ICache> childCaches = cacheToChildCaches.get(currentParentCache);
		if (childCaches == null)
		{
			childCaches = new ArrayList<ICache>();
			cacheToChildCaches.put(currentParentCache, childCaches);
		}
		childCaches.add(childCache);
	}

	protected CacheWalkerResult buildWalkedEntry(ICache cache, IObjRef[] objRefs, IdentityHashMap<ICache, List<ICache>> cacheToChildCaches,
			IdentityHashMap<ICache, ICache> cacheToProxyCache)
	{
		List<ICache> childCaches = cacheToChildCaches.get(cache);
		CacheWalkerResult[] childCacheEntries;

		if (childCaches == null)
		{
			childCacheEntries = null;
		}
		else
		{
			childCacheEntries = new CacheWalkerResult[childCaches.size()];
			for (int a = childCaches.size(); a-- > 0;)
			{
				childCacheEntries[a] = buildWalkedEntry(childCaches.get(a), objRefs, cacheToChildCaches, cacheToProxyCache);
			}
		}
		ICache proxyCache = cacheToProxyCache.get(cache);

		boolean transactional = false, threadLocal = false;
		if (proxyCache != null)
		{
			threadLocal = true;
			if (transactionState != null && transactionState.isTransactionActive())
			{
				transactional = true;
			}
		}

		List<Object> cacheValues;
		if (objRefs != allEntityRefs)
		{
			if (cache instanceof ChildCache)
			{
				cacheValues = cache.getObjects(objRefs, EnumSet.of(CacheDirective.FailEarly, CacheDirective.ReturnMisses));
			}
			else
			{
				cacheValues = cache.getObjects(objRefs, EnumSet.of(CacheDirective.FailEarly, CacheDirective.CacheValueResult, CacheDirective.ReturnMisses));
			}
		}
		else
		{
			final IdentityHashSet<Object> fCacheValues = new IdentityHashSet<Object>();
			cache.getContent(new HandleContentDelegate()
			{
				@Override
				public void invoke(Class<?> entityType, byte idIndex, Object id, Object value)
				{
					fCacheValues.add(value);
				}
			});
			cacheValues = fCacheValues.toList();

			// generate ad-hoc objRefs
			objRefs = cacheValues.size() > 0 ? objRefHelper.extractObjRefList(cacheValues, null).toArray(IObjRef.class) : ObjRef.EMPTY_ARRAY;
		}
		Object childEntries = childCacheEntries;
		if (childCacheEntries != null && childCacheEntries.length == 1)
		{
			childEntries = childCacheEntries[0];
		}
		CacheWalkerResult parentEntry = new CacheWalkerResult(cache, transactional, threadLocal, objRefs, cacheValues.toArray(), childEntries);
		if (childCacheEntries != null)
		{
			for (int a = childCacheEntries.length; a-- > 0;)
			{
				childCacheEntries[a].setParentEntry(parentEntry);
			}
		}
		if (objRefs != allEntityRefs)
		{
			parentEntry.updatePendingChanges();
		}
		return parentEntry;
	}
}
