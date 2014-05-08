package de.osthus.ambeth.util;

import java.util.List;

import de.osthus.ambeth.collections.IMap;

public interface ICachePathHelper
{
	void buildCachePath(Class<?> entityType, String memberToInitialize, List<CachePath> cachePaths);

	<V extends List<CachePath>> IPrefetchState ensureInitializedRelations(Object objects, IMap<Class<?>, V> typeToMembersToInitialize);
}
