package de.osthus.ambeth.xml.namehandler;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.xml.INameBasedHandler;
import de.osthus.ambeth.xml.IReader;
import de.osthus.ambeth.xml.IWriter;
import de.osthus.ambeth.xml.typehandler.AbstractHandler;

public class TimeSpanElementHandler extends AbstractHandler implements INameBasedHandler
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public boolean writesCustom(Object obj, Class<?> type, IWriter writer)
	{
		// TimeSpan does not exist in Java
		return false;
	}

	@Override
	public Object readObject(Class<?> returnType, String elementName, int id, IReader reader)
	{
		String spanString = reader.getAttributeValue(xmlDictionary.getValueAttribute());
		return conversionHelper.convertValueToType(Long.class, spanString);
	}
}
