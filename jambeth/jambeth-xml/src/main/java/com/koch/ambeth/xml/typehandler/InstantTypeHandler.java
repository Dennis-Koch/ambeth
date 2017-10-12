package com.koch.ambeth.xml.typehandler;

import java.time.Instant;

import com.koch.ambeth.xml.IReader;
import com.koch.ambeth.xml.ITypeBasedHandler;
import com.koch.ambeth.xml.IWriter;

public class InstantTypeHandler extends AbstractHandler implements ITypeBasedHandler {
	@Override
	public void writeObject(Object obj, Class<?> type, IWriter writer) {
		writer.writeAttribute(xmlDictionary.getValueAttribute(), ((Instant) obj).toEpochMilli());
	}

	@Override
	public Object readObject(Class<?> returnType, Class<?> objType, int id, IReader reader) {
		return Instant
				.ofEpochMilli(Long.parseLong(reader.getAttributeValue(xmlDictionary.getValueAttribute())));
	}
}
