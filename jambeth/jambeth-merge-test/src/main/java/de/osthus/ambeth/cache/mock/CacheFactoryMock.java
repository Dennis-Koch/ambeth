package de.osthus.ambeth.cache.mock;

import de.osthus.ambeth.cache.CacheFactoryDirective;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheFactory;
import de.osthus.ambeth.cache.IDisposableCache;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

/**
 * Support for unit tests that do not include jAmbeth.Cache
 */
public class CacheFactoryMock implements ICacheFactory
{
	@Override
	public IDisposableCache withParent(ICache parent, IResultingBackgroundWorkerDelegate<IDisposableCache> runnable)
	{
		return null;
	}

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