package de.osthus.ambeth.service;

public interface ICacheServiceByNameExtendable
{
	void registerCacheService(ICacheService cacheService, String serviceName);

	void unregisterCacheService(ICacheService cacheService, String serviceName);
}