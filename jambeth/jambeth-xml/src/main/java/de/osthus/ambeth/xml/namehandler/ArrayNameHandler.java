package de.osthus.ambeth.xml.namehandler;

import java.lang.reflect.Array;
import java.util.regex.Pattern;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.converter.EncodingInformation;
import de.osthus.ambeth.xml.ICyclicXmlDictionary;
import de.osthus.ambeth.xml.INameBasedHandler;
import de.osthus.ambeth.xml.IReader;
import de.osthus.ambeth.xml.IWriter;
import de.osthus.ambeth.xml.pending.ICommandBuilder;
import de.osthus.ambeth.xml.pending.ICommandTypeRegistry;
import de.osthus.ambeth.xml.pending.IObjectCommand;
import de.osthus.ambeth.xml.pending.IObjectFuture;
import de.osthus.ambeth.xml.typehandler.AbstractHandler;

public class ArrayNameHandler extends AbstractHandler implements INameBasedHandler, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	public static final String primitiveValueSeparator = ";";

	protected static final Pattern splitPattern = Pattern.compile(primitiveValueSeparator);

	protected ICommandBuilder commandBuilder;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(commandBuilder, "commandBuilder");
	}

	public void setCommandBuilder(ICommandBuilder commandBuilder)
	{
		this.commandBuilder = commandBuilder;
	}

	@Override
	public boolean writesCustom(Object obj, Class<?> type, IWriter writer)
	{
		if (!type.isArray())
		{
			return false;
		}
		IConversionHelper conversionHelper = this.conversionHelper;
		ICyclicXmlDictionary xmlDictionary = this.xmlDictionary;
		String arrayElement = xmlDictionary.getArrayElement();
		writer.writeStartElement(arrayElement);
		int id = writer.acquireIdForObject(obj);
		writer.writeAttribute(xmlDictionary.getIdAttribute(), Integer.toString(id));
		int length = Array.getLength(obj);
		writer.writeAttribute(xmlDictionary.getSizeAttribute(), Integer.toString(length));
		Class<?> componentType = type.getComponentType();
		classElementHandler.writeAsAttribute(componentType, writer);
		if (length == 0)
		{
			writer.writeEndElement();
		}
		else
		{
			writer.writeStartElementEnd();
			if (componentType.isPrimitive())
			{
				writer.write("<values v=\"");
				if (char.class.equals(componentType) || byte.class.equals(componentType) || boolean.class.equals(componentType))
				{
					String value = conversionHelper.convertValueToType(String.class, obj, EncodingInformation.SOURCE_PLAIN | EncodingInformation.TARGET_BASE64);
					writer.write(value);
				}
				else
				{
					for (int a = 0; a < length; a++)
					{
						Object item = Array.get(obj, a);
						if (a > 0)
						{
							writer.write(primitiveValueSeparator);
						}
						String value = conversionHelper.convertValueToType(String.class, item);
						writer.writeEscapedXml(value);
					}
				}
				writer.write("\"/>");
			}
			else
			{
				for (int a = 0; a < length; a++)
				{
					Object item = Array.get(obj, a);
					writer.writeObject(item);
				}
			}
			writer.writeCloseElement(arrayElement);
		}
		return true;
	}

	@Override
	public Object readObject(Class<?> returnType, String elementName, int id, IReader reader)
	{
		IConversionHelper conversionHelper = this.conversionHelper;
		ICyclicXmlDictionary xmlDictionary = this.xmlDictionary;
		if (!xmlDictionary.getArrayElement().equals(elementName))
		{
			throw new IllegalStateException("Element '" + elementName + "' not supported");
		}
		int length = Integer.parseInt(reader.getAttributeValue(xmlDictionary.getSizeAttribute()));
		Class<?> componentType = classElementHandler.readFromAttribute(reader);

		Object targetArray;
		if (!reader.isEmptyElement())
		{
			reader.nextTag();
		}
		if ("values".equals(reader.getElementName()))
		{
			String listOfValuesString = reader.getAttributeValue("v");
			if (char.class.equals(componentType) || byte.class.equals(componentType) || boolean.class.equals(componentType))
			{
				targetArray = Array.newInstance(componentType, 0);
				targetArray = conversionHelper.convertValueToType(targetArray.getClass(), listOfValuesString, EncodingInformation.SOURCE_BASE64
						| EncodingInformation.TARGET_PLAIN);
				reader.putObjectWithId(targetArray, id);
			}
			else
			{
				targetArray = Array.newInstance(componentType, length);
				reader.putObjectWithId(targetArray, id);
				String[] items = splitPattern.split(listOfValuesString);
				for (int a = 0, size = items.length; a < size; a++)
				{
					String item = items[a];
					if (item == null || item.length() == 0)
					{
						continue;
					}
					Object convertedValue = conversionHelper.convertValueToType(componentType, items[a]);
					Array.set(targetArray, a, convertedValue);
				}
			}
			reader.moveOverElementEnd();
		}
		else
		{
			targetArray = Array.newInstance(componentType, length);
			reader.putObjectWithId(targetArray, id);
			ICommandTypeRegistry commandTypeRegistry = reader.getCommandTypeRegistry();
			ICommandBuilder commandBuilder = this.commandBuilder;
			for (int index = 0; index < length; index++)
			{
				Object item = reader.readObject();
				if (item instanceof IObjectFuture)
				{
					IObjectFuture objectFuture = (IObjectFuture) item;
					IObjectCommand command = commandBuilder.build(commandTypeRegistry, objectFuture, targetArray, index);
					reader.addObjectCommand(command);
				}
				else
				{
					Array.set(targetArray, index, item);
				}
			}
		}
		return targetArray;
	}
}
