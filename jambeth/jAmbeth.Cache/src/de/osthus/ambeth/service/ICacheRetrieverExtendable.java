package de.osthus.ambeth.service;

public interface ICacheRetrieverExtendable
{
	void registerCacheRetriever(ICacheRetriever cacheRetriever, Class<?> handledType);

	void unregisterCacheRetriever(ICacheRetriever cacheRetriever, Class<?> handledType);
}
