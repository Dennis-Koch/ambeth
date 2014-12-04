package de.osthus.ambeth.cache.interceptor;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheProvider;

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