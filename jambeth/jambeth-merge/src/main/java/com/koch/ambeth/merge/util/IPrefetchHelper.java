package com.koch.ambeth.merge.util;

import java.util.List;

import com.koch.ambeth.util.collections.IList;

public interface IPrefetchHelper
{
	IPrefetchConfig createPrefetch();

	IPrefetchState prefetch(Object objects);

	<T, S> IList<T> extractTargetEntities(List<S> sourceEntities, String sourceToTargetEntityPropertyPath, Class<S> sourceEntityType);
}
