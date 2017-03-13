package com.koch.ambeth.cache.interceptor;

import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheProvider;
import com.koch.ambeth.merge.cache.IDisposableCache;
import com.koch.ambeth.util.IDisposable;

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