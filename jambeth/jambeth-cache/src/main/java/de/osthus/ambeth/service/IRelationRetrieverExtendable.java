package de.osthus.ambeth.service;

public interface IRelationRetrieverExtendable
{
	void registerRelationRetriever(IRelationRetriever relationRetriever, Class<?> handledType, String propertyName);

	void unregisterRelationRetriever(IRelationRetriever relationRetriever, Class<?> handledType, String propertyName);
}
