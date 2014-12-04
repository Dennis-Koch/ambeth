package de.osthus.ambeth.xml;

import de.osthus.ambeth.cache.CacheFactoryDirective;
import de.osthus.ambeth.cache.ICacheFactory;
import de.osthus.ambeth.cache.IDisposableCache;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class CacheFactoryDummy implements ICacheFactory
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public IDisposableCache createPrivileged(CacheFactoryDirective cacheFactoryDirective, String name)
	{
		return null;
	}

	@Override
	public IDisposableCache createPrivileged(CacheFactoryDirective cacheFactoryDirective, boolean foreignThreadAware, Boolean useWeakEntries, String name)
	{
		return null;
	}

	@Override
	public IDisposableCache create(CacheFactoryDirective cacheFactoryDirective, String name)
	{
		return null;
	}

	@Override
	public IDisposableCache create(CacheFactoryDirective cacheFactoryDirective, boolean foreignThreadAware, Boolean useWeakEntries, String name)
	{
		return null;
	}
}
