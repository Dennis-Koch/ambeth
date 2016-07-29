package de.osthus.ambeth.xml;

import de.osthus.ambeth.collections.AbstractTuple2KeyHashMap;

public interface IXmlTypeRegistry
{
	Class<?> getType(String name, String namespace);

	IXmlTypeKey getXmlType(Class<?> type);

	IXmlTypeKey getXmlType(Class<?> type, boolean expectExisting);

	AbstractTuple2KeyHashMap<String, String, Class<?>> createSnapshot();
}