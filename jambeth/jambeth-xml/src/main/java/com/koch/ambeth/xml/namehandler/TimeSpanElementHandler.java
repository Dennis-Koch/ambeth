package com.koch.ambeth.xml.namehandler;

import com.koch.ambeth.xml.INameBasedHandler;
import com.koch.ambeth.xml.IReader;
import com.koch.ambeth.xml.IWriter;
import com.koch.ambeth.xml.typehandler.AbstractHandler;

public class TimeSpanElementHandler extends AbstractHandler implements INameBasedHandler {
	@Override
	public boolean writesCustom(Object obj, Class<?> type, IWriter writer) {
		// TimeSpan does not exist in Java
		return false;
	}

	@Override
	public Object readObject(Class<?> returnType, String elementName, int id, IReader reader) {
		String spanString = reader.getAttributeValue(xmlDictionary.getValueAttribute());
		return conversionHelper.convertValueToType(Long.class, spanString);
	}
}
