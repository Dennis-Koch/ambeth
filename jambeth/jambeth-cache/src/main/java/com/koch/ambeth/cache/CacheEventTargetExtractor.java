package com.koch.ambeth.cache;

import com.koch.ambeth.event.IEventTargetExtractor;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.cache.ICacheProvider;

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
