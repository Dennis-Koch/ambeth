package com.koch.ambeth.xml;

/*-
 * #%L
 * jambeth-xml
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


public interface IWriter
{
	boolean isInAttributeState();

	void writeEscapedXml(CharSequence unescapedString);

	void writeAttribute(CharSequence attributeName, Object attributeValue);

	void writeAttribute(CharSequence attributeName, CharSequence attributeValue);

	void writeEndElement();

	void writeCloseElement(CharSequence elementName);

	void write(CharSequence s);

	void writeOpenElement(CharSequence elementName);

	void writeStartElement(CharSequence elementName);

	void writeStartElementEnd();

	void writeObject(Object obj);

	void writeEmptyElement(CharSequence elementName);

	void write(char s);

	int getIdOfObject(Object obj);

	int acquireIdForObject(Object obj);

	void putMembersOfType(Class<?> type, SpecifiedMember[] members);

	SpecifiedMember[] getMembersOfType(Class<?> type);

	void addSubstitutedEntity(Object entity);
}
