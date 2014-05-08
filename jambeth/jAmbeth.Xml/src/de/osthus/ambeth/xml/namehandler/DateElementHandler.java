package de.osthus.ambeth.xml.namehandler;

import java.util.Date;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.xml.INameBasedHandler;
import de.osthus.ambeth.xml.IReader;
import de.osthus.ambeth.xml.IWriter;
import de.osthus.ambeth.xml.typehandler.AbstractHandler;

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
