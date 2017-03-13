package com.koch.ambeth.cache.mock;

import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheProvider;

/**
 * Support for unit tests that do not include jAmbeth.Cache
 */
public class CacheProviderMock implements ICacheProvider
{
	@Override
	public ICache getCurrentCache()
	{
		return null;
	}

	@Override
	public boolean isNewInstanceOnCall()
	{
		return false;
	}
}
