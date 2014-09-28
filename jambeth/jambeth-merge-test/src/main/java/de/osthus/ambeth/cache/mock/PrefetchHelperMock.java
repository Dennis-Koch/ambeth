package de.osthus.ambeth.cache.mock;

import java.util.List;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.util.IPrefetchConfig;
import de.osthus.ambeth.util.IPrefetchHelper;
import de.osthus.ambeth.util.IPrefetchState;

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
