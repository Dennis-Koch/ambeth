package de.osthus.ambeth.util;

import java.util.List;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;

public interface IPrefetchHelper
{
	IPrefetchConfig createPrefetch();

	IPrefetchState prefetch(Object objects);

	IPrefetchState prefetch(Object objects, IMap<Class<?>, List<String>> typeToMembersToInitialize);

	<T, S> IList<T> extractTargetEntities(List<S> sourceEntities, String sourceToTargetEntityPropertyPath, Class<S> sourceEntityType);
}
