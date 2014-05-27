package de.osthus.ambeth.cache;

public interface ICacheProviderExtendable
{
	void registerCacheProvider(ICacheProvider cacheProvider);

	void unregisterCacheProvider(ICacheProvider cacheProvider);
}
