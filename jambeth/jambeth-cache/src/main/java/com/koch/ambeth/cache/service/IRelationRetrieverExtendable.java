package com.koch.ambeth.cache.service;

public interface IRelationRetrieverExtendable
{
	void registerRelationRetriever(IRelationRetriever relationRetriever, Class<?> handledType, String propertyName);

	void unregisterRelationRetriever(IRelationRetriever relationRetriever, Class<?> handledType, String propertyName);
}
