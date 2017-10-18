package com.koch.ambeth.xml.typehandler;

import java.util.regex.Pattern;

import com.koch.ambeth.xml.IReader;
import com.koch.ambeth.xml.ITypeBasedHandler;
import com.koch.ambeth.xml.IWriter;

public class PatternTypeHandler extends AbstractHandler implements ITypeBasedHandler {
	@Override
	public void writeObject(Object obj, Class<?> type, IWriter writer) {
		writer.writeAttribute(xmlDictionary.getValueAttribute(), ((Pattern) obj).pattern());
	}

	@Override
	public Object readObject(Class<?> returnType, Class<?> objType, int id, IReader reader) {
		return Pattern.compile(reader.getAttributeValue(xmlDictionary.getValueAttribute()));
	}
}
