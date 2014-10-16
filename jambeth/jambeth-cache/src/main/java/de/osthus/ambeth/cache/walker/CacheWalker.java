package de.osthus.ambeth.cache.walker;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.ChildCache;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheIntern;
import de.osthus.ambeth.cache.IRootCache;
import de.osthus.ambeth.cache.RootCache;
import de.osthus.ambeth.collections.ArrayList;
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
import de.osthus.ambeth.proxy.IValueHolderContainer;

public class CacheWalker implements ICacheWalker
{
	@LogInstance
	private ILogger log;

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
				ICacheIntern targetCache = ((IValueHolderContainer) entity).get__TargetCache();
				if (targetCache != null)
				{
					allCachesSet.add(targetCache);
				}
			}
		}

		return walkIntern(objRefs.toArray(IObjRef.class), allCachesSet);
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

		allCachesSet.add(this.firstLevelCache.getCurrentCache());

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
				parent = ((RootCache) currentParent).getParent();
				child = currentParent;
			}
			checkParentCache(committedRootCache, currentCommittedRootCache, child, cacheToChildCaches, cacheToProxyCache);
		}
		HashSet<IObjRef> objRefsSet = new HashSet<IObjRef>(objRefs);
		IObjRef[] objRefsArray = objRefsSet.toArray(IObjRef.class);

		Arrays.sort(objRefsArray, new Comparator<IObjRef>()
		{
			@Override
			public int compare(IObjRef o1, IObjRef o2)
			{
				int result = o1.getRealType().getName().compareTo(o2.getRealType().getName());
				if (result != 0)
				{
					return result;
				}
				result = o1.getIdNameIndex() == o2.getIdNameIndex() ? 0 : o1.getIdNameIndex() > o2.getIdNameIndex() ? 1 : -1;
				if (result != 0)
				{
					return result;
				}
				return o1.getId().toString().compareTo(o2.getId().toString());
			}
		});
		return buildWalkedEntry(currentCommittedRootCache, objRefsArray, cacheToChildCaches, cacheToProxyCache);
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

		List<Object> cacheValues = null;
		if (cache instanceof ChildCache)
		{
			cacheValues = cache.getObjects(objRefs, EnumSet.of(CacheDirective.FailEarly, CacheDirective.ReturnMisses));
		}
		else
		{
			cacheValues = cache.getObjects(objRefs, EnumSet.of(CacheDirective.FailEarly, CacheDirective.CacheValueResult, CacheDirective.ReturnMisses));
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
		return parentEntry;
	}
}
