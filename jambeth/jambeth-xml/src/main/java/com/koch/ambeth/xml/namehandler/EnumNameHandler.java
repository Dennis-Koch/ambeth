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

import java.lang.reflect.Method;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.xml.INameBasedHandler;
import com.koch.ambeth.xml.IReader;
import com.koch.ambeth.xml.IWriter;
import com.koch.ambeth.xml.typehandler.AbstractHandler;

public class EnumNameHandler extends AbstractHandler implements INameBasedHandler {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final Method enumValueOf;

	public EnumNameHandler() {
		try {
			enumValueOf = Enum.class.getMethod("valueOf", Class.class, String.class);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public boolean writesCustom(Object obj, Class<?> type, IWriter writer) {
		if (!type.isEnum()) {
			return false;
		}
		writer.writeStartElement(xmlDictionary.getEnumElement());
		int id = writer.acquireIdForObject(obj);
		writer.writeAttribute(xmlDictionary.getIdAttribute(), Integer.toString(id));
		classElementHandler.writeAsAttribute(type, writer);
		writer.writeAttribute(xmlDictionary.getValueAttribute(), obj.toString());
		writer.writeEndElement();
		return true;
	}

	@Override
	public Object readObject(Class<?> returnType, String elementName, int id, IReader reader) {
		if (!xmlDictionary.getEnumElement().equals(elementName)) {
			throw new IllegalStateException("Element '" + elementName + "' not supported");
		}
		Class<?> enumType = classElementHandler.readFromAttribute(reader);

		String enumValue = reader.getAttributeValue(xmlDictionary.getValueAttribute());
		if (enumValue == null) {
			throw new IllegalStateException("Element '" + elementName + "' invalid");
		}
		try {
			return enumValueOf.invoke(null, enumType, enumValue);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
