package com.koch.ambeth.cache;

import com.koch.ambeth.merge.cache.ICacheProvider;

public interface ICacheProviderExtendable
{
	void registerCacheProvider(ICacheProvider cacheProvider);

	void unregisterCacheProvider(ICacheProvider cacheProvider);
}
