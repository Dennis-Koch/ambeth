package com.koch.ambeth.cache.interceptor;

import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheProvider;

public class SingleCacheProvider implements ICacheProvider
{
	protected final ICache cache;

	public SingleCacheProvider(ICache cache)
	{
		this.cache = cache;
	}

	@Override
	public ICache getCurrentCache()
	{
		return cache;
	}

	@Override
	public boolean isNewInstanceOnCall()
	{
		return false;
	}
}