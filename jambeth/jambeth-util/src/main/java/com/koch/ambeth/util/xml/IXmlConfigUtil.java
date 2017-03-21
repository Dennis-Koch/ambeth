package com.koch.ambeth.util.xml;

/*-
 * #%L
 * jambeth-util
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public interface IXmlConfigUtil
{
	Document[] readXmlFiles(String xmlFileNames);

	IXmlValidator createValidator(String... xsdFileNames);

	String readDocumentNamespace(Document doc);

	IList<Element> nodesToElements(NodeList nodeList);

	IMap<String, IList<Element>> childrenToElementMap(Node parent);

	IMap<String, IList<Element>> toElementMap(NodeList nodeList);

	IList<Element> getElementsByName(String name, IMap<String, IList<Element>> elementMap);

	Element getChildUnique(Element parent, String childTagName);

	String getAttribute(Element entityTag, String type);

	String getRequiredAttribute(Element element, String attrName);

	String getRequiredAttribute(Element element, String attrName, boolean firstToUpper);

	String getChildElementAttribute(Element node, String childName, String attrName, String error);

	boolean attributeIsTrue(Element element, String attrName);

	Class<?> getTypeForName(String name);

	Class<?> getTypeForName(String name, boolean tryOnly);
}
