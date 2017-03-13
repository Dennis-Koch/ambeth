package com.koch.ambeth.cache.mock;

import java.util.List;

import com.koch.ambeth.merge.util.IPrefetchConfig;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.merge.util.IPrefetchState;
import com.koch.ambeth.util.collections.IList;

/**
 * Support for unit tests that do not include jAmbeth.Cache
 */
public class PrefetchHelperMock implements IPrefetchHelper
{
	@Override
	public IPrefetchConfig createPrefetch()
	{
		return null;
	}

	@Override
	public IPrefetchState prefetch(Object objects)
	{
		return null;
	}

	@Override
	public <T, S> IList<T> extractTargetEntities(List<S> sourceEntities, String sourceToTargetEntityPropertyPath, Class<S> sourceEntityType)
	{
		return null;
	}
}
