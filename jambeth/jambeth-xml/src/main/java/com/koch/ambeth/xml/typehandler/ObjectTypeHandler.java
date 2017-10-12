package com.koch.ambeth.xml.typehandler;

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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.typeinfo.ITypeInfoItem;
import com.koch.ambeth.xml.IReader;
import com.koch.ambeth.xml.ITypeBasedHandler;
import com.koch.ambeth.xml.IWriter;
import com.koch.ambeth.xml.SpecifiedMember;
import com.koch.ambeth.xml.pending.ICommandBuilder;
import com.koch.ambeth.xml.pending.ICommandTypeRegistry;
import com.koch.ambeth.xml.pending.IObjectCommand;
import com.koch.ambeth.xml.pending.IObjectFuture;

public class ObjectTypeHandler extends AbstractHandler implements ITypeBasedHandler {
	@Autowired
	protected ICommandBuilder commandBuilder;

	@Override
	public void writeObject(Object obj, Class<?> type, IWriter writer) {
		writer.writeStartElementEnd();
		SpecifiedMember[] members = writer.getMembersOfType(type);

		String unspecifiedElement = xmlDictionary.getUnspecifiedElement();
		String valueAttribute = xmlDictionary.getValueAttribute();
		String primitiveElement = xmlDictionary.getPrimitiveElement();
		for (int a = 0, size = members.length; a < size; a++) {
			SpecifiedMember specifiedMember = members[a];
			if (specifiedMember.getSpecifiedMember() != null) {
				boolean specified = conversionHelper.convertValueToType(boolean.class,
						specifiedMember.getSpecifiedMember().getValue(obj, true));
				if (!specified) {
					writer.writeStartElement(unspecifiedElement);
					writer.writeEndElement();
					continue;
				}
			}
			ITypeInfoItem member = specifiedMember.getMember();

			Object fieldValue = member.getValue(obj, false);
			if (member.getRealType().isPrimitive()) {
				writer.writeStartElement(primitiveElement);
				String convertedValue = conversionHelper.convertValueToType(String.class, fieldValue);
				writer.writeAttribute(valueAttribute, convertedValue);
				writer.writeEndElement();
				continue;
			}
			writer.writeObject(fieldValue);
		}
	}

	@Override
	public Object readObject(Class<?> returnType, Class<?> objType, int id, IReader reader) {
		Object obj;
		try {
			obj = objType.newInstance();
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		if (id > 0) {
			reader.putObjectWithId(obj, id);
		}
		reader.nextTag();
		SpecifiedMember[] members = reader.getMembersOfType(objType);

		int index = 0;
		String valueAttribute = xmlDictionary.getValueAttribute();
		String unspecifiedElement = xmlDictionary.getUnspecifiedElement();
		String primitiveElement = xmlDictionary.getPrimitiveElement();
		ICommandBuilder commandBuilder = this.commandBuilder;
		ICommandTypeRegistry commandTypeRegistry = reader.getCommandTypeRegistry();
		IConversionHelper conversionHelper = this.conversionHelper;
		while (reader.isStartTag()) {
			String elementName = reader.getElementName();
			SpecifiedMember specifiedMember = members[index++];
			ITypeInfoItem specMember = specifiedMember.getSpecifiedMember();
			if (unspecifiedElement.equals(elementName)) {
				reader.moveOverElementEnd();
				if (specMember != null) {
					specMember.setValue(obj, Boolean.FALSE);
				}
				continue;
			}
			if (specMember != null) {
				specMember.setValue(obj, Boolean.TRUE);
			}
			ITypeInfoItem member = specifiedMember.getMember();
			Object memberValue;
			if (primitiveElement.equals(elementName)) {
				String value = reader.getAttributeValue(valueAttribute);
				memberValue = conversionHelper.convertValueToType(member.getRealType(), value);
				reader.moveOverElementEnd();
			}
			else {
				memberValue = reader.readObject(member.getRealType());
			}
			if (specifiedMember.getSpecifiedMember() != null) {
				specifiedMember.getSpecifiedMember().setValue(obj, Boolean.TRUE);
			}
			if (memberValue instanceof IObjectFuture) {
				IObjectFuture objectFuture = (IObjectFuture) memberValue;
				IObjectCommand command =
						commandBuilder.build(commandTypeRegistry, objectFuture, obj, member);
				reader.addObjectCommand(command);
			}
			else {
				member.setValue(obj, memberValue);
			}
		}
		return obj;
	}
}
