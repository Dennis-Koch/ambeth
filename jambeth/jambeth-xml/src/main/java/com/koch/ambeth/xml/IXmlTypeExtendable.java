package com.koch.ambeth.xml;

public interface IXmlTypeExtendable
{
	void registerXmlType(Class<?> type, String name, String namespace);

	void unregisterXmlType(Class<?> type, String name, String namespace);
}
