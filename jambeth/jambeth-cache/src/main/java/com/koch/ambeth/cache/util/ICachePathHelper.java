package com.koch.ambeth.cache.util;

import com.koch.ambeth.merge.util.IPrefetchState;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.ISet;

public interface ICachePathHelper
{
	void buildCachePath(Class<?> entityType, String memberToInitialize, ISet<AppendableCachePath> cachePaths);

	IPrefetchState ensureInitializedRelations(Object objects, ILinkedMap<Class<?>, PrefetchPath[]> entityTypeToPrefetchSteps);

	AppendableCachePath copyCachePathToAppendable(PrefetchPath cachePath);

	PrefetchPath[] copyAppendableToCachePath(ISet<AppendableCachePath> children);

	PrefetchPath copyAppendableToCachePath(AppendableCachePath cachePath);

	void unionCachePath(AppendableCachePath cachePath, AppendableCachePath other);
}
