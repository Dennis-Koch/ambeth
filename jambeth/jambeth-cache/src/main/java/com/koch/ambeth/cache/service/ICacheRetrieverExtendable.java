package com.koch.ambeth.cache.service;

public interface ICacheRetrieverExtendable
{
	void registerCacheRetriever(ICacheRetriever cacheRetriever, Class<?> handledType);

	void unregisterCacheRetriever(ICacheRetriever cacheRetriever, Class<?> handledType);
}
