package de.osthus.ambeth.xml;

import java.util.List;

public interface IXmlTypeHelper
{
	String getXmlName(Class<?> valueObjectType);

	String getXmlNamespace(Class<?> valueObjectType);

	String getXmlTypeName(Class<?> valueObjectType);

	Class<?> getType(String xmlName);

	Class<?>[] getTypes(List<String> xmlNames);
}
