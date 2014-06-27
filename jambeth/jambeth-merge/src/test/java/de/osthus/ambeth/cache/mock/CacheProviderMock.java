package de.osthus.ambeth.cache.mock;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheProvider;

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

	@Override
	public ICache getCurrentCacheIfAvailable()
	{
		return null;
	}
}
