package de.osthus.ambeth.xml.typehandler;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.xml.IReader;
import de.osthus.ambeth.xml.ITypeBasedHandler;
import de.osthus.ambeth.xml.IWriter;
import de.osthus.ambeth.xml.pending.ICommandBuilder;
import de.osthus.ambeth.xml.pending.ICommandTypeRegistry;
import de.osthus.ambeth.xml.pending.IObjectCommand;
import de.osthus.ambeth.xml.pending.IObjectFuture;

public class ObjectTypeHandler extends AbstractHandler implements ITypeBasedHandler
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICommandBuilder commandBuilder;

	@Override
	public void writeObject(Object obj, Class<?> type, IWriter writer)
	{
		writer.writeStartElementEnd();
		ITypeInfoItem[] members = writer.getMembersOfType(type);

		String valueAttribute = xmlDictionary.getValueAttribute();
		String primitiveElement = xmlDictionary.getPrimitiveElement();
		for (int a = 0, size = members.length; a < size; a++)
		{
			ITypeInfoItem field = members[a];

			Object fieldValue = field.getValue(obj, false);
			if (field.getRealType().isPrimitive())
			{
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
	public Object readObject(Class<?> returnType, Class<?> objType, int id, IReader reader)
	{
		Object obj;
		try
		{
			obj = objType.newInstance();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		if (id > 0)
		{
			reader.putObjectWithId(obj, id);
		}
		reader.nextTag();
		ITypeInfoItem[] members = reader.getMembersOfType(objType);

		int index = 0;
		String valueAttribute = xmlDictionary.getValueAttribute();
		String primitiveElement = xmlDictionary.getPrimitiveElement();
		ICommandBuilder commandBuilder = this.commandBuilder;
		ICommandTypeRegistry commandTypeRegistry = reader.getCommandTypeRegistry();
		IConversionHelper conversionHelper = this.conversionHelper;
		while (reader.isStartTag())
		{
			ITypeInfoItem member = members[index++];
			Object memberValue;
			if (primitiveElement.equals(reader.getElementName()))
			{
				String value = reader.getAttributeValue(valueAttribute);
				memberValue = conversionHelper.convertValueToType(member.getRealType(), value);
				reader.moveOverElementEnd();
			}
			else
			{
				memberValue = reader.readObject(member.getRealType());
			}
			if (memberValue instanceof IObjectFuture)
			{
				IObjectFuture objectFuture = (IObjectFuture) memberValue;
				IObjectCommand command = commandBuilder.build(commandTypeRegistry, objectFuture, obj);
				reader.addObjectCommand(command);
			}
			else
			{
				member.setValue(obj, memberValue);
			}
		}
		return obj;
	}
}
