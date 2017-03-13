package com.koch.ambeth.cache.service;

public interface ICacheServiceByNameExtendable
{
	void registerCacheService(ICacheService cacheService, String serviceName);

	void unregisterCacheService(ICacheService cacheService, String serviceName);
}