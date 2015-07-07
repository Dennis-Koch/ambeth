package de.osthus.ambeth.util;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.ISet;

public interface ICachePathHelper
{
	void buildCachePath(Class<?> entityType, String memberToInitialize, ISet<AppendableCachePath> cachePaths);

	IPrefetchState ensureInitializedRelations(Object objects, ILinkedMap<Class<?>, PrefetchPath[]> entityTypeToPrefetchSteps);

	AppendableCachePath copyCachePathToAppendable(PrefetchPath cachePath);

	PrefetchPath[] copyAppendableToCachePath(ISet<AppendableCachePath> children);

	PrefetchPath copyAppendableToCachePath(AppendableCachePath cachePath);

	void unionCachePath(AppendableCachePath cachePath, AppendableCachePath other);
}
