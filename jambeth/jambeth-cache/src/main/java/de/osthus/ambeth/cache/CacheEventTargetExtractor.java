package de.osthus.ambeth.cache;

import de.osthus.ambeth.event.IEventTargetExtractor;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class CacheEventTargetExtractor implements IEventTargetExtractor, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
	}

	@Override
	public Object extractEventTarget(Object eventTarget)
	{
		Class<?> targetType = eventTarget.getClass();
		if (ChildCache.class.equals(targetType) || RootCache.class.equals(targetType))
		{
			return eventTarget;
		}
		if (ICacheProvider.class.isAssignableFrom(targetType))
		{
			ICacheProvider cacheProvider = (ICacheProvider) eventTarget;
			if (cacheProvider.isNewInstanceOnCall())
			{
				return null;
			}
			return cacheProvider.getCurrentCache();
		}
		return eventTarget;
	}
}
