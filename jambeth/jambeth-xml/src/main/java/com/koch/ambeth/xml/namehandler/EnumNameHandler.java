package com.koch.ambeth.xml.namehandler;

import java.lang.reflect.Method;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.xml.INameBasedHandler;
import com.koch.ambeth.xml.IReader;
import com.koch.ambeth.xml.IWriter;
import com.koch.ambeth.xml.typehandler.AbstractHandler;

public class EnumNameHandler extends AbstractHandler implements INameBasedHandler
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final Method enumValueOf;

	public EnumNameHandler()
	{
		try
		{
			enumValueOf = Enum.class.getMethod("valueOf", Class.class, String.class);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public boolean writesCustom(Object obj, Class<?> type, IWriter writer)
	{
		if (!type.isEnum())
		{
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
	public Object readObject(Class<?> returnType, String elementName, int id, IReader reader)
	{
		if (!xmlDictionary.getEnumElement().equals(elementName))
		{
			throw new IllegalStateException("Element '" + elementName + "' not supported");
		}
		Class<?> enumType = classElementHandler.readFromAttribute(reader);

		String enumValue = reader.getAttributeValue(xmlDictionary.getValueAttribute());
		if (enumValue == null)
		{
			throw new IllegalStateException("Element '" + elementName + "' invalid");
		}
		try
		{
			return enumValueOf.invoke(null, enumType, enumValue);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
