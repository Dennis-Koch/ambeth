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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.xml.INameBasedHandler;
import com.koch.ambeth.xml.IReader;
import com.koch.ambeth.xml.IWriter;
import com.koch.ambeth.xml.IXmlTypeRegistry;
import com.koch.ambeth.xml.pending.ICommandBuilder;
import com.koch.ambeth.xml.pending.ICommandTypeRegistry;
import com.koch.ambeth.xml.pending.IObjectCommand;
import com.koch.ambeth.xml.pending.IObjectFuture;
import com.koch.ambeth.xml.typehandler.AbstractHandler;

public class CollectionElementHandler extends AbstractHandler implements INameBasedHandler {
	@Autowired
	protected ICommandBuilder commandBuilder;

	@Autowired
	protected IXmlTypeRegistry xmlTypeRegistry;

	protected Class<?> getComponentTypeOfCollection(Object obj) {
		return Object.class;
	}

	@Override
	public boolean writesCustom(Object obj, Class<?> type, IWriter writer) {
		if (!Collection.class.isAssignableFrom(type)) {
			return false;
		}
		Collection<?> coll = (Collection<?>) obj;
		String collElement;
		if (Set.class.isAssignableFrom(type)) {
			collElement = xmlDictionary.getSetElement();
		}
		else if (List.class.isAssignableFrom(type)) {
			collElement = xmlDictionary.getListElement();
		}
		else {
			throw new IllegalStateException("Collection of type " + type.getName() + " not supported");
		}
		writer.writeStartElement(collElement);
		int id = writer.acquireIdForObject(obj);
		writer.writeAttribute(xmlDictionary.getIdAttribute(), Integer.toString(id));
		int length = coll.size();
		writer.writeAttribute(xmlDictionary.getSizeAttribute(), Integer.toString(length));

		Class<?> componentType = getComponentTypeOfCollection(obj);
		classElementHandler.writeAsAttribute(componentType, writer);
		writer.writeStartElementEnd();
		Iterator<?> iter = coll.iterator();
		while (iter.hasNext()) {
			Object item = iter.next();
			writer.writeObject(item);
		}
		writer.writeCloseElement(collElement);
		return true;
	}

	@Override
	public Object readObject(Class<?> returnType, String elementName, int id, IReader reader) {
		if (!xmlDictionary.getSetElement().equals(elementName)
				&& !xmlDictionary.getListElement().equals(elementName)) {
			throw new IllegalStateException("Element '" + elementName + "' not supported");
		}
		String lengthValue = reader.getAttributeValue(xmlDictionary.getSizeAttribute());
		int length = lengthValue != null && lengthValue.length() > 0 ? Integer.parseInt(lengthValue)
				: 0;

		// Read componentType because of typeId registration. This is intended to remain unused in java
		@SuppressWarnings("unused")
		Class<?> componentType = classElementHandler.readFromAttribute(reader);

		Collection<Object> coll;
		if (xmlDictionary.getSetElement().equals(elementName)) {
			coll = length > 0 ? new HashSet<>((int) (length / 0.75f + 1), 0.75f) : new HashSet<>();
		}
		else {
			coll = length > 0 ? new ArrayList<>(length) : new ArrayList<>();
		}
		reader.putObjectWithId(coll, id);
		reader.nextTag();
		boolean useObjectFuture = false;
		ICommandBuilder commandBuilder = this.commandBuilder;
		ICommandTypeRegistry commandTypeRegistry = reader.getCommandTypeRegistry();
		while (reader.isStartTag()) {
			Object item = reader.readObject();
			if (item instanceof IObjectFuture) {
				IObjectFuture objectFuture = (IObjectFuture) item;
				IObjectCommand command = commandBuilder.build(commandTypeRegistry, objectFuture, coll);
				reader.addObjectCommand(command);
				useObjectFuture = true;
			}
			else if (useObjectFuture) {
				IObjectCommand command = commandBuilder.build(commandTypeRegistry, null, coll, item);
				reader.addObjectCommand(command);
			}
			else {
				coll.add(item);
			}
		}
		return coll;
	}
}
