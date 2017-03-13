package com.koch.ambeth.cache.service;

public interface IPrimitiveRetrieverExtendable
{
	void registerPrimitiveRetriever(IPrimitiveRetriever primitiveRetriever, Class<?> handledType, String propertyName);

	void unregisterPrimitiveRetriever(IPrimitiveRetriever primitiveRetriever, Class<?> handledType, String propertyName);
}
