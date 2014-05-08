package de.osthus.ambeth.util.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;

public interface IXmlConfigUtil
{
	Document[] readXmlFiles(String xmlFileNames);

	IXmlValidator createValidator(String... xsdFileNames);

	String readDocumentNamespace(Document doc);

	IList<Element> nodesToElements(NodeList nodeList);

	IMap<String, IList<Element>> childrenToElementMap(Element parent);

	IMap<String, IList<Element>> toElementMap(NodeList nodeList);

	Element getChildUnique(Element parent, String childTagName);

	String getAttribute(Element entityTag, String type);

	String getRequiredAttribute(Element element, String attrName);

	String getRequiredAttribute(Element element, String attrName, boolean firstToUpper);

	String getChildElementAttribute(Element node, String childName, String attrName, String error);

	boolean attributeIsTrue(Element element, String attrName);

	Class<?> getTypeForName(String name);

	Class<?> getTypeForName(String name, boolean tryOnly);
}