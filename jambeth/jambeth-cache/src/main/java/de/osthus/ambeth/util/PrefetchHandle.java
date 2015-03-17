package de.osthus.ambeth.util;

import java.util.Map.Entry;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.collections.LinkedHashSet;

public class PrefetchHandle implements IPrefetchHandle
{
	protected final ILinkedMap<Class<?>, CachePath[]> entityTypeToPrefetchSteps;

	protected final ICachePathHelper cachePathHelper;

	public PrefetchHandle(ILinkedMap<Class<?>, CachePath[]> entityTypeToPrefetchSteps, ICachePathHelper cachePathHelper)
	{
		this.entityTypeToPrefetchSteps = entityTypeToPrefetchSteps;
		this.cachePathHelper = cachePathHelper;
	}

	public ILinkedMap<Class<?>, CachePath[]> getEntityTypeToPrefetchSteps()
	{
		return entityTypeToPrefetchSteps;
	}

	@Override
	public IPrefetchState prefetch(Object objects)
	{
		return cachePathHelper.ensureInitializedRelations(objects, entityTypeToPrefetchSteps);
	}

	@Override
	public IPrefetchState prefetch(Object... objects)
	{
		return cachePathHelper.ensureInitializedRelations(objects, entityTypeToPrefetchSteps);
	}

	@Override
	public IPrefetchHandle union(IPrefetchHandle otherPrefetchHandle)
	{
		if (otherPrefetchHandle == null)
		{
			return this;
		}
		LinkedHashMap<Class<?>, LinkedHashSet<AppendableCachePath>> newMap = LinkedHashMap.create(entityTypeToPrefetchSteps.size());
		for (Entry<Class<?>, CachePath[]> entry : entityTypeToPrefetchSteps)
		{
			LinkedHashSet<AppendableCachePath> prefetchPaths = newMap.get(entry.getKey());
			if (prefetchPaths == null)
			{
				prefetchPaths = new LinkedHashSet<AppendableCachePath>();
				newMap.put(entry.getKey(), prefetchPaths);
			}
			for (CachePath cachePath : entry.getValue())
			{
				prefetchPaths.add(cachePathHelper.copyCachePathToAppendable(cachePath));
			}
		}
		for (Entry<Class<?>, CachePath[]> entry : ((PrefetchHandle) otherPrefetchHandle).entityTypeToPrefetchSteps)
		{
			LinkedHashSet<AppendableCachePath> prefetchPaths = newMap.get(entry.getKey());
			if (prefetchPaths == null)
			{
				prefetchPaths = new LinkedHashSet<AppendableCachePath>();
				newMap.put(entry.getKey(), prefetchPaths);
			}
			for (CachePath cachePath : entry.getValue())
			{
				AppendableCachePath clonedCachePath = cachePathHelper.copyCachePathToAppendable(cachePath);
				if (prefetchPaths.add(clonedCachePath))
				{
					continue;
				}
				AppendableCachePath existingCachePath = prefetchPaths.get(clonedCachePath);
				cachePathHelper.unionCachePath(existingCachePath, clonedCachePath);
			}
		}
		LinkedHashMap<Class<?>, CachePath[]> targetMap = LinkedHashMap.create(newMap.size());
		for (Entry<Class<?>, LinkedHashSet<AppendableCachePath>> entry : newMap)
		{
			CachePath[] cachePaths = cachePathHelper.copyAppendableToCachePath(entry.getValue());
			targetMap.put(entry.getKey(), cachePaths);
		}
		return new PrefetchHandle(targetMap, cachePathHelper);
	}
}
