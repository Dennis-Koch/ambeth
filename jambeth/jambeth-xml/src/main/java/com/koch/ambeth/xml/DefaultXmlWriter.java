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

import java.io.Writer;

import com.koch.ambeth.ioc.util.ImmutableTypeSet;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.appendable.WriterAppendable;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.xml.postprocess.IPostProcessWriter;

public class DefaultXmlWriter implements IWriter, IPostProcessWriter {
	protected final IAppendable appendable;

	protected final ICyclicXmlController xmlController;

	protected final IdentityHashMap<Object, Integer> mutableToIdMap = new IdentityHashMap<>();
	protected final HashMap<Object, Integer> immutableToIdMap = new HashMap<>();
	protected int nextIdMapIndex = 1;

	protected IdentityHashSet<Object> substitutedEntities;

	protected final HashMap<Class<?>, SpecifiedMember[]> typeToMemberMap = new HashMap<>();

	protected boolean isInAttributeState = false;

	protected int beautifierLevel;

	protected boolean beautifierIgnoreLineBreak = true;

	protected int elementContentLevel = -1;

	protected String beautifierLinebreak;

	protected boolean beautifierActive;

	public DefaultXmlWriter(final Writer osw, ICyclicXmlController xmlController) {
		this(new WriterAppendable(osw), xmlController);
	}

	public DefaultXmlWriter(IAppendable appendable, ICyclicXmlController xmlController) {
		this.appendable = appendable;
		this.xmlController = xmlController;
	}

	public void setBeautifierActive(boolean beautifierActive) {
		this.beautifierActive = beautifierActive;
	}

	public boolean isBeautifierActive() {
		return beautifierActive;
	}

	public String getBeautifierLinebreak() {
		return beautifierLinebreak;
	}

	public void setBeautifierLinebreak(String beautifierLinebreak) {
		this.beautifierLinebreak = beautifierLinebreak;
	}

	protected void writeBeautifierTabs(int amount) {
		if (beautifierIgnoreLineBreak) {
			beautifierIgnoreLineBreak = false;
		}
		else {
			write(beautifierLinebreak);
		}
		while (amount-- > 0) {
			write('\t');
		}
	}

	@Override
	public void writeEscapedXml(CharSequence unescapedString) {
		for (int a = 0, size = unescapedString.length(); a < size; a++) {
			char oneChar = unescapedString.charAt(a);
			switch (oneChar) {
				case '&':
					appendable.append("&amp;");
					break;
				case '\"':
					appendable.append("&quot;");
					break;
				case '\'':
					appendable.append("&apos;");
					break;
				case '<':
					appendable.append("&lt;");
					break;
				case '>':
					appendable.append("&gt;");
					break;
				default:
					appendable.append(oneChar);
					break;
			}
		}
	}

	@Override
	public void writeAttribute(CharSequence attributeName, Object attributeValue) {
		if (attributeValue == null) {
			return;
		}
		writeAttribute(attributeName, attributeValue.toString());
	}

	@Override
	public void writeAttribute(CharSequence attributeName, CharSequence attributeValue) {
		if (attributeValue == null || attributeValue.length() == 0) {
			return;
		}
		checkIfInAttributeState();
		appendable.append(' ').append(attributeName).append("=\"");
		writeEscapedXml(attributeValue);
		appendable.append('\"');
	}

	@Override
	public void writeIntAttribute(CharSequence attributeName, int attributeValue) {
		checkIfInAttributeState();
		appendable.append(' ').append(attributeName).append("=\"");
		appendable.appendInt(attributeValue);
		appendable.append('\"');
	}

	@Override
	public void writeEndElement() {
		checkIfInAttributeState();
		appendable.append("/>");
		isInAttributeState = false;
		if (beautifierActive) {
			beautifierLevel--;
		}
	}

	@Override
	public void writeCloseElement(CharSequence elementName) {
		if (isInAttributeState) {
			writeEndElement();
			isInAttributeState = false;
		}
		else {
			if (beautifierActive) {
				if (elementContentLevel == beautifierLevel) {
					writeBeautifierTabs(beautifierLevel - 1);
				}
				beautifierLevel--;
				elementContentLevel = beautifierLevel;
			}
			appendable.append("</").append(elementName).append('>');
		}
	}

	@Override
	public void write(CharSequence s) {
		appendable.append(s);
	}

	@Override
	public void writeOpenElement(CharSequence elementName) {
		endTagIfInAttributeState();
		if (beautifierActive) {
			writeBeautifierTabs(beautifierLevel);
			appendable.append('<').append(elementName).append('>');
			elementContentLevel = beautifierLevel;
			beautifierLevel++;
		}
		else {
			appendable.append('<').append(elementName).append('>');
		}
	}

	@Override
	public void writeStartElement(CharSequence elementName) {
		endTagIfInAttributeState();
		if (beautifierActive) {
			writeBeautifierTabs(beautifierLevel);
			appendable.append('<').append(elementName);
			elementContentLevel = beautifierLevel;
			beautifierLevel++;
		}
		else {
			appendable.append('<').append(elementName);
		}
		isInAttributeState = true;
	}

	@Override
	public void writeStartElementEnd() {
		if (!isInAttributeState) {
			return;
		}
		checkIfInAttributeState();
		appendable.append('>');
		isInAttributeState = false;
	}

	@Override
	public void writeObject(Object obj) {
		xmlController.writeObject(obj, this);
	}

	@Override
	public void writeEmptyElement(CharSequence elementName) {
		endTagIfInAttributeState();
		if (beautifierActive) {
			elementContentLevel = beautifierLevel - 1;
			writeBeautifierTabs(beautifierLevel);
		}
		appendable.append('<').append(elementName).append("/>");
	}

	@Override
	public void write(char s) {
		appendable.append(s);
	}

	@Override
	public int acquireIdForObject(Object obj) {
		boolean isImmutableType = ImmutableTypeSet.isImmutableType(obj.getClass());
		IMap<Object, Integer> objectToIdMap = isImmutableType ? immutableToIdMap : mutableToIdMap;

		Integer id = Integer.valueOf(nextIdMapIndex++);
		if (!objectToIdMap.putIfNotExists(obj, id)) {
			throw new IllegalStateException("There is already a id mapped given object (" + obj + ")");
		}

		return id.intValue();
	}

	@Override
	public int getIdOfObject(Object obj) {
		boolean isImmutableType = ImmutableTypeSet.isImmutableType(obj.getClass());
		IMap<Object, Integer> objectToIdMap = isImmutableType ? immutableToIdMap : mutableToIdMap;

		Integer id = objectToIdMap.get(obj);

		return (id == null) ? 0 : id.intValue();
	}

	@Override
	public void putMembersOfType(Class<?> type, SpecifiedMember[] members) {
		if (!typeToMemberMap.putIfNotExists(type, members)) {
			throw new IllegalStateException("Already mapped type '" + type + "'");
		}
	}

	@Override
	public SpecifiedMember[] getMembersOfType(Class<?> type) {
		return typeToMemberMap.get(type);
	}

	@Override
	public ISet<Object> getSubstitutedEntities() {
		return substitutedEntities;
	}

	@Override
	public void addSubstitutedEntity(Object entity) {
		if (substitutedEntities == null) {
			substitutedEntities = new IdentityHashSet<>();
		}
		substitutedEntities.add(entity);
	}

	@Override
	public IMap<Object, Integer> getMutableToIdMap() {
		return mutableToIdMap;
	}

	@Override
	public IMap<Object, Integer> getImmutableToIdMap() {
		return immutableToIdMap;
	}

	@Override
	public boolean isInAttributeState() {
		return isInAttributeState;
	}

	protected void checkIfInAttributeState() {
		if (!isInAttributeState()) {
			throw new IllegalStateException("There is currently no pending open tag to attribute");
		}
	}

	protected void endTagIfInAttributeState() {
		if (isInAttributeState()) {
			writeStartElementEnd();
		}
	}
}
