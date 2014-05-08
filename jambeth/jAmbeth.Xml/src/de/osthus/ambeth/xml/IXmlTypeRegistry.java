package de.osthus.ambeth.xml;

public interface IXmlTypeRegistry
{
	Class<?> getType(String name, String namespace);

	IXmlTypeKey getXmlType(Class<?> type);

	IXmlTypeKey getXmlType(Class<?> type, boolean expectExisting);
}