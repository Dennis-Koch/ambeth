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

import java.util.List;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.extendable.ClassExtendableContainer;
import com.koch.ambeth.ioc.extendable.MapExtendableContainer;
import com.koch.ambeth.merge.IProxyHelper;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IntKeyMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.xml.typehandler.AbstractHandler;

public class CyclicXmlController extends AbstractHandler
		implements ICyclicXmlController, ITypeBasedHandlerExtendable, INameBasedHandlerExtendable {
	@Autowired
	protected IProxyHelper proxyHelper;

	@Autowired
	protected IXmlTypeRegistry xmlTypeRegistry;

	protected final ClassExtendableContainer<ITypeBasedHandler> typeToElementHandlers =
			new ClassExtendableContainer<>(
					"elementHandler", "type");

	protected final MapExtendableContainer<String, INameBasedHandler> nameBasedElementReaders =
			new MapExtendableContainer<>(
					"nameBasedElementReader", "elementName");

	protected final List<INameBasedHandler> nameBasedElementWriters = new ArrayList<>();

	@Override
	public void registerElementHandler(ITypeBasedHandler elementHandler, Class<?> type) {
		typeToElementHandlers.register(elementHandler, type);
	}

	@Override
	public void unregisterElementHandler(ITypeBasedHandler elementHandler, Class<?> type) {
		typeToElementHandlers.unregister(elementHandler, type);
	}

	@Override
	public void registerNameBasedElementHandler(INameBasedHandler nameBasedElementHandler,
			String elementName) {
		nameBasedElementReaders.register(nameBasedElementHandler, elementName);
		nameBasedElementWriters.add(nameBasedElementHandler);
	}

	@Override
	public void unregisterNameBasedElementHandler(INameBasedHandler nameBasedElementHandler,
			String elementName) {
		nameBasedElementReaders.unregister(nameBasedElementHandler, elementName);
		nameBasedElementWriters.remove(nameBasedElementHandler);
	}

	@Override
	public Object readObject(IReader reader) {
		return readObject(Object.class, reader);
	}

	@Override
	public Object readObject(Class<?> returnType, IReader reader) {
		String elementName = reader.getElementName();
		if (xmlDictionary.getNullElement().equals(elementName)) {
			reader.moveOverElementEnd();
			return null;
		}
		String idValue = reader.getAttributeValue(xmlDictionary.getIdAttribute());
		int id = idValue != null && idValue.length() > 0 ? Integer.parseInt(idValue) : 0;
		try {
			if (xmlDictionary.getRefElement().equals(elementName)) {
				reader.moveOverElementEnd();
				return reader.getObjectById(id);
			}
			Object obj;
			if (xmlDictionary.getObjectElement().equals(elementName)) {
				Class<?> type = classElementHandler.readFromAttribute(reader);
				obj = readObjectContent(returnType, type, id, reader);
			}
			else {
				INameBasedHandler nameBasedElementReader =
						nameBasedElementReaders.getExtension(elementName);
				if (nameBasedElementReader == null) {
					throw new IllegalStateException("Element name '" + elementName + "' not supported");
				}
				obj = nameBasedElementReader.readObject(returnType, elementName, id, reader);
			}
			if (id > 0) {
				reader.putObjectWithId(obj, id);
			}
			reader.moveOverElementEnd();
			return obj;
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e,
					"Error occured while processing element '" + elementName + "' with id '" + id + "'");
		}
	}

	@Override
	public void writeObject(Object obj, IWriter writer) {
		if (obj == null) {
			writer.writeStartElement(xmlDictionary.getNullElement());
			writer.writeEndElement();
			return;
		}
		Integer idValue = writer.getIdOfObject(obj);
		if (idValue > 0) {
			writer.writeStartElement(xmlDictionary.getRefElement());
			writer.writeAttribute(xmlDictionary.getIdAttribute(), idValue.toString());
			writer.writeEndElement();
			return;
		}
		Class<?> type = proxyHelper.getRealType(obj.getClass());
		for (int a = 0, size = nameBasedElementWriters.size(); a < size; a++) {
			INameBasedHandler nameBasedElementWriter = nameBasedElementWriters.get(a);
			if (nameBasedElementWriter.writesCustom(obj, type, writer)) {
				return;
			}
		}
		int id = writer.acquireIdForObject(obj);
		String objectElement = xmlDictionary.getObjectElement();
		writer.writeStartElement(objectElement);
		if (id > 0) {
			writer.writeAttribute(xmlDictionary.getIdAttribute(), Integer.toString(id));
		}
		classElementHandler.writeAsAttribute(type, writer);
		writeObjectContent(obj, type, writer);
		writer.writeCloseElement(objectElement);
	}

	protected Object readObjectContent(Class<?> returnType, Class<?> type, int id, IReader reader) {
		ITypeBasedHandler extension = typeToElementHandlers.getExtension(type);
		if (extension == null) {
			throw new IllegalStateException("No extension mapped to type '" + type.getName() + "' found");
		}
		return extension.readObject(returnType, type, id, reader);
	}

	protected void writeObjectContent(Object obj, Class<?> type, IWriter writer) {
		ITypeBasedHandler extension = typeToElementHandlers.getExtension(type);
		if (extension == null) {
			throw new IllegalStateException("No extension mapped to type '" + type.getName() + "' found");
		}
		extension.writeObject(obj, type, writer);
	}

	protected Object resolveObjectById(IntKeyMap<Object> idToObjectMap, int id) {
		Object obj = idToObjectMap.get(id);
		if (obj == null) {
			throw new IllegalStateException("No object found with id " + id);
		}
		return obj;
	}
}
