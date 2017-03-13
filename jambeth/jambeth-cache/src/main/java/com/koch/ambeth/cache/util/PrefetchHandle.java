package com.koch.ambeth.cache.util;

import java.util.Map.Entry;

import com.koch.ambeth.merge.util.IPrefetchHandle;
import com.koch.ambeth.merge.util.IPrefetchState;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.LinkedHashSet;

public class PrefetchHandle implements IPrefetchHandle
{
	protected final ILinkedMap<Class<?>, PrefetchPath[]> entityTypeToPrefetchSteps;

	protected final ICachePathHelper cachePathHelper;

	public PrefetchHandle(ILinkedMap<Class<?>, PrefetchPath[]> entityTypeToPrefetchSteps, ICachePathHelper cachePathHelper)
	{
		this.entityTypeToPrefetchSteps = entityTypeToPrefetchSteps;
		this.cachePathHelper = cachePathHelper;
	}

	public ILinkedMap<Class<?>, PrefetchPath[]> getEntityTypeToPrefetchSteps()
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
		for (Entry<Class<?>, PrefetchPath[]> entry : entityTypeToPrefetchSteps)
		{
			LinkedHashSet<AppendableCachePath> prefetchPaths = newMap.get(entry.getKey());
			if (prefetchPaths == null)
			{
				prefetchPaths = new LinkedHashSet<AppendableCachePath>();
				newMap.put(entry.getKey(), prefetchPaths);
			}
			for (PrefetchPath cachePath : entry.getValue())
			{
				prefetchPaths.add(cachePathHelper.copyCachePathToAppendable(cachePath));
			}
		}
		for (Entry<Class<?>, PrefetchPath[]> entry : ((PrefetchHandle) otherPrefetchHandle).entityTypeToPrefetchSteps)
		{
			LinkedHashSet<AppendableCachePath> prefetchPaths = newMap.get(entry.getKey());
			if (prefetchPaths == null)
			{
				prefetchPaths = new LinkedHashSet<AppendableCachePath>();
				newMap.put(entry.getKey(), prefetchPaths);
			}
			for (PrefetchPath cachePath : entry.getValue())
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
		LinkedHashMap<Class<?>, PrefetchPath[]> targetMap = LinkedHashMap.create(newMap.size());
		for (Entry<Class<?>, LinkedHashSet<AppendableCachePath>> entry : newMap)
		{
			PrefetchPath[] cachePaths = cachePathHelper.copyAppendableToCachePath(entry.getValue());
			targetMap.put(entry.getKey(), cachePaths);
		}
		return new PrefetchHandle(targetMap, cachePathHelper);
	}
}
