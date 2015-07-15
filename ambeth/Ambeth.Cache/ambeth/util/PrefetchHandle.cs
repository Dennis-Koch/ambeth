using De.Osthus.Ambeth.Collections;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Util
{
    public class PrefetchHandle : IPrefetchHandle
    {
        protected readonly ILinkedMap<Type, PrefetchPath[]> entityTypeToPrefetchSteps;

	    protected readonly ICachePathHelper cachePathHelper;

	    public PrefetchHandle(ILinkedMap<Type, PrefetchPath[]> entityTypeToPrefetchSteps, ICachePathHelper cachePathHelper)
	    {
		    this.entityTypeToPrefetchSteps = entityTypeToPrefetchSteps;
		    this.cachePathHelper = cachePathHelper;
	    }

        public ILinkedMap<Type, PrefetchPath[]> GetEntityTypeToPrefetchSteps()
	    {
		    return entityTypeToPrefetchSteps;
	    }

	    public IPrefetchState Prefetch(Object objects)
	    {
		    return cachePathHelper.EnsureInitializedRelations(objects, entityTypeToPrefetchSteps);
	    }

	    public IPrefetchState Prefetch(params Object[] objects)
	    {
		    return cachePathHelper.EnsureInitializedRelations(objects, entityTypeToPrefetchSteps);
	    }

	    public IPrefetchHandle Union(IPrefetchHandle otherPrefetchHandle)
	    {
		    if (otherPrefetchHandle == null)
		    {
			    return this;
		    }
            LinkedHashMap<Type, CHashSet<AppendableCachePath>> newMap = LinkedHashMap<Type, CHashSet<AppendableCachePath>>.Create(entityTypeToPrefetchSteps.Count);
		    foreach (Entry<Type, PrefetchPath[]> entry in entityTypeToPrefetchSteps)
		    {
                CHashSet<AppendableCachePath> prefetchPaths = newMap.Get(entry.Key);
			    if (prefetchPaths == null)
			    {
                    prefetchPaths = new CHashSet<AppendableCachePath>();
				    newMap.Put(entry.Key, prefetchPaths);
			    }
			    foreach (PrefetchPath cachePath in entry.Value)
			    {
				    prefetchPaths.Add(cachePathHelper.CopyCachePathToAppendable(cachePath));
			    }
		    }
		    foreach (Entry<Type, PrefetchPath[]> entry in ((PrefetchHandle) otherPrefetchHandle).entityTypeToPrefetchSteps)
		    {
                CHashSet<AppendableCachePath> prefetchPaths = newMap.Get(entry.Key);
			    if (prefetchPaths == null)
			    {
                    prefetchPaths = new CHashSet<AppendableCachePath>();
				    newMap.Put(entry.Key, prefetchPaths);
			    }
			    foreach (PrefetchPath cachePath in entry.Value)
			    {
				    AppendableCachePath clonedCachePath = cachePathHelper.CopyCachePathToAppendable(cachePath);
				    if (prefetchPaths.Add(clonedCachePath))
				    {
					    continue;
				    }
				    AppendableCachePath existingCachePath = prefetchPaths.Get(clonedCachePath);
				    cachePathHelper.UnionCachePath(existingCachePath, clonedCachePath);
			    }
		    }
		    LinkedHashMap<Type, PrefetchPath[]> targetMap = LinkedHashMap<Type, PrefetchPath[]>.Create(newMap.Count);
            foreach (Entry<Type, CHashSet<AppendableCachePath>> entry in newMap)
		    {
			    PrefetchPath[] cachePaths = cachePathHelper.CopyAppendableToCachePath(entry.Value);
			    targetMap.Put(entry.Key, cachePaths);
		    }
		    return new PrefetchHandle(targetMap, cachePathHelper);
	    }
    }
}