package de.osthus.ambeth.xml.namehandler;

import java.lang.reflect.Method;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.xml.INameBasedHandler;
import de.osthus.ambeth.xml.IReader;
import de.osthus.ambeth.xml.IWriter;
import de.osthus.ambeth.xml.typehandler.AbstractHandler;

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
