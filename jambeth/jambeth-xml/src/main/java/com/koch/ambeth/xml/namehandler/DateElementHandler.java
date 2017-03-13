package com.koch.ambeth.xml.namehandler;

import java.util.Date;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.xml.INameBasedHandler;
import com.koch.ambeth.xml.IReader;
import com.koch.ambeth.xml.IWriter;
import com.koch.ambeth.xml.typehandler.AbstractHandler;

public class DateElementHandler extends AbstractHandler implements INameBasedHandler
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public boolean writesCustom(Object obj, Class<?> type, IWriter writer)
	{
		if (!Date.class.isAssignableFrom(type))
		{
			return false;
		}
		int id = writer.acquireIdForObject(obj);
		long time = ((Date) obj).getTime();
		writer.writeStartElement("d");
		writer.writeAttribute(xmlDictionary.getIdAttribute(), Integer.toString(id));
		writer.writeAttribute(xmlDictionary.getValueAttribute(), Long.toString(time));
		writer.writeEndElement();
		return true;
	}

	@Override
	public Object readObject(Class<?> returnType, String elementName, int id, IReader reader)
	{
		if (!"d".equals(elementName))
		{
			throw new IllegalStateException("Element '" + elementName + "' not supported");
		}
		String timeString = reader.getAttributeValue(xmlDictionary.getValueAttribute());
		return new Date(Long.parseLong(timeString));
	}
}
