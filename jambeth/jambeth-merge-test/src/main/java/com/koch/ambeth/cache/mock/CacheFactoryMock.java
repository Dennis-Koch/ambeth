package com.koch.ambeth.cache.mock;

import com.koch.ambeth.merge.cache.CacheFactoryDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.merge.cache.IDisposableCache;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

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