package de.osthus.ambeth.xml;

import java.util.List;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.IPrefetchConfig;
import de.osthus.ambeth.util.IPrefetchHelper;
import de.osthus.ambeth.util.IPrefetchState;

public class PrefetchHelperDummy implements IPrefetchHelper
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

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
