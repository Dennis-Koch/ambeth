package de.osthus.ambeth.util;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.ISet;

public interface ICachePathHelper
{
	void buildCachePath(Class<?> entityType, String memberToInitialize, ISet<AppendableCachePath> cachePaths);

	IPrefetchState ensureInitializedRelations(Object objects, ILinkedMap<Class<?>, CachePath[]> entityTypeToPrefetchSteps);

	AppendableCachePath copyCachePathToAppendable(CachePath cachePath);

	CachePath[] copyAppendableToCachePath(ISet<AppendableCachePath> children);

	CachePath copyAppendableToCachePath(AppendableCachePath cachePath);

	void unionCachePath(AppendableCachePath cachePath, AppendableCachePath other);
}
