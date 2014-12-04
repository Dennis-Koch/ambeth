package de.osthus.ambeth.cache.interceptor;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheProvider;
import de.osthus.ambeth.cache.IDisposableCache;
import de.osthus.ambeth.util.IDisposable;

public abstract class SingleCacheOnDemandProvider implements ICacheProvider, IDisposable
{
	protected ICache cache;

	@Override
	protected void finalize() throws Throwable
	{
		dispose();
	}

	@Override
	public void dispose()
	{
		if (cache instanceof IDisposableCache)
		{
			((IDisposableCache) cache).dispose();
		}
	}

	@Override
	public ICache getCurrentCache()
	{
		if (cache == null)
		{
			cache = resolveCurrentCache();
		}
		return cache;
	}

	protected abstract ICache resolveCurrentCache();

	@Override
	public boolean isNewInstanceOnCall()
	{
		return false;
	}
}