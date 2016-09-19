package de.osthus.ambeth.xml;

import de.osthus.ambeth.collections.AbstractTuple2KeyHashMap;

public interface IXmlTypeRegistry
{
	Class<?> getType(String name, String namespace);

	/**
	 * Provides a valid name if the given one is null, empty or "##default"
	 * 
	 * @param type
	 * @param providedName
	 * @return
	 */
	String getXmlTypeName(Class<?> type, String providedName);

	/**
	 * Provides a valid namespace if the given one is empty or "##default"
	 * 
	 * @param type
	 * @param providedNamespace
	 * @return
	 */
	String getXmlTypeNamespace(Class<?> type, String providedNamespace);

	IXmlTypeKey getXmlType(Class<?> type);

	IXmlTypeKey getXmlType(Class<?> type, boolean expectExisting);

	AbstractTuple2KeyHashMap<String, String, Class<?>> createSnapshot();
}