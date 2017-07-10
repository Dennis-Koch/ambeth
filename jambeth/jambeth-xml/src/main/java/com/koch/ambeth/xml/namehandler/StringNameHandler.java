package com.koch.ambeth.xml.namehandler;

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

import java.util.regex.Pattern;

import com.koch.ambeth.xml.INameBasedHandler;
import com.koch.ambeth.xml.IReader;
import com.koch.ambeth.xml.IWriter;
import com.koch.ambeth.xml.typehandler.AbstractHandler;

public class StringNameHandler extends AbstractHandler implements INameBasedHandler {
	private static final String emptyString = "";

	private static final String cdataStartSeq = "<![CDATA[";

	private static final String cdataEndSeq = "]]>";

	protected static final Pattern cdataPattern = Pattern.compile(Pattern.quote(cdataEndSeq));

	@Override
	public boolean writesCustom(Object obj, Class<?> type, IWriter writer) {
		if (!String.class.equals(type)) {
			return false;
		}
		String value = (String) obj;

		String stringElement = xmlDictionary.getStringElement();
		writer.writeStartElement(stringElement);
		int id = writer.acquireIdForObject(obj);
		writer.writeAttribute(xmlDictionary.getIdAttribute(), Integer.toString(id));

		if (value.isEmpty()) {
			writer.writeEndElement();
			return true;
		}
		writer.writeStartElementEnd();

		String[] parts = cdataPattern.split(value);
		if (parts.length == 1) {
			writer.write(cdataStartSeq);
			writer.write(value);
			writer.write(cdataEndSeq);
		}
		else {
			// First part
			writer.writeOpenElement(stringElement);
			writer.write(cdataStartSeq);
			writer.write(parts[0]);
			writer.write("]]");
			writer.write(cdataEndSeq);
			writer.writeCloseElement(stringElement);

			// All parts in between
			int lastIndex = parts.length - 1;
			for (int i = 1; i < lastIndex; i++) {
				writer.writeOpenElement(stringElement);
				writer.write(cdataStartSeq);
				writer.write('>');
				writer.write(parts[i]);
				writer.write("]]");
				writer.write(cdataEndSeq);
				writer.writeCloseElement(stringElement);
			}

			// Last part
			writer.writeOpenElement(stringElement);
			writer.write(cdataStartSeq);
			writer.write('>');
			writer.write(parts[lastIndex]);
			writer.write(cdataEndSeq);
			writer.writeCloseElement(stringElement);
		}

		writer.writeCloseElement(stringElement);

		return true;
	}

	@Override
	public Object readObject(Class<?> returnType, String elementName, int id, IReader reader) {
		if (!xmlDictionary.getStringElement().equals(elementName)) {
			throw new IllegalStateException("Element '" + elementName + "' not supported");
		}
		if (reader.isEmptyElement()) {
			return emptyString;
		}
		reader.nextToken();
		StringBuilder sb = null;
		try {
			if (!reader.isStartTag()) {
				String value = reader.getElementValue();
				reader.nextTag();
				if (value == null) {
					value = emptyString;
				}
				return value;
			}
			while (reader.isStartTag()) {
				if (!"s".equals(reader.getElementName())) {
					throw new IllegalStateException("Element '" + elementName + "' not supported");
				}
				if (sb == null) {
					sb = objectCollector.create(StringBuilder.class);
				}
				reader.nextToken();
				String textPart = reader.getElementValue();
				sb.append(textPart);
				reader.nextToken();
				reader.moveOverElementEnd();
			}
			return sb.toString();
		}
		finally {
			if (sb != null) {
				objectCollector.dispose(sb);
			}
		}
	}
}
