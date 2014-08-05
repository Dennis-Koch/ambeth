package de.osthus.ambeth.cache.mock;

import de.osthus.ambeth.cache.CacheFactoryDirective;
import de.osthus.ambeth.cache.ICacheFactory;
import de.osthus.ambeth.cache.IDisposableCache;

/**
 * Support for unit tests that do not include jAmbeth.Cache
 */
public class CacheFactoryMock implements ICacheFactory
{
	@Override
	public IDisposableCache createPrivileged(CacheFactoryDirective cacheFactoryDirective)
	{
		return null;
	}

	@Override
	public IDisposableCache createPrivileged(CacheFactoryDirective cacheFactoryDirective, boolean foreignThreadAware, Boolean useWeakEntries)
	{
		return null;
	}

	@Override
	public IDisposableCache create(CacheFactoryDirective cacheFactoryDirective)
	{
		return null;
	}

	@Override
	public IDisposableCache create(CacheFactoryDirective cacheFactoryDirective, boolean foreignThreadAware, Boolean useWeakEntries)
	{
		return null;
	}
}