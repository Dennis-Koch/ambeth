package de.osthus.ambeth.service;

public interface IPropertyCacheRetrieverExtendable
{
	void registerPropertyCacheRetriever(IPropertyCacheRetriever propertyCacheRetriever, Class<?> handledType, String propertyName);

	void unregisterPropertyCacheRetriever(IPropertyCacheRetriever propertyCacheRetriever, Class<?> handledType, String propertyName);
}
