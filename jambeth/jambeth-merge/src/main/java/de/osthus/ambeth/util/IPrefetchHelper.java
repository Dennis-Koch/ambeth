package de.osthus.ambeth.util;

import java.util.List;

import de.osthus.ambeth.collections.IList;

public interface IPrefetchHelper
{
	IPrefetchConfig createPrefetch();

	IPrefetchState prefetch(Object objects);

	<T, S> IList<T> extractTargetEntities(List<S> sourceEntities, String sourceToTargetEntityPropertyPath, Class<S> sourceEntityType);
}
