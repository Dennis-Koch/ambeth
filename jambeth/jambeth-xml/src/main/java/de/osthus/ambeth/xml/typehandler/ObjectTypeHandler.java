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
import de.osthus.ambeth.xml.SpecifiedMember;
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
		SpecifiedMember[] members = writer.getMembersOfType(type);

		String unspecifiedElement = xmlDictionary.getUnspecifiedElement();
		String valueAttribute = xmlDictionary.getValueAttribute();
		String primitiveElement = xmlDictionary.getPrimitiveElement();
		for (int a = 0, size = members.length; a < size; a++)
		{
			SpecifiedMember specifiedMember = members[a];
			if (specifiedMember.getSpecifiedMember() != null)
			{
				boolean specified = conversionHelper.convertValueToType(boolean.class, specifiedMember.getSpecifiedMember().getValue(obj, true));
				if (!specified)
				{
					writer.writeStartElement(unspecifiedElement);
					writer.writeEndElement();
					continue;
				}
			}
			ITypeInfoItem member = specifiedMember.getMember();

			Object fieldValue = member.getValue(obj, false);
			if (member.getRealType().isPrimitive())
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
		SpecifiedMember[] members = reader.getMembersOfType(objType);

		int index = 0;
		String valueAttribute = xmlDictionary.getValueAttribute();
		String unspecifiedElement = xmlDictionary.getUnspecifiedElement();
		String primitiveElement = xmlDictionary.getPrimitiveElement();
		ICommandBuilder commandBuilder = this.commandBuilder;
		ICommandTypeRegistry commandTypeRegistry = reader.getCommandTypeRegistry();
		IConversionHelper conversionHelper = this.conversionHelper;
		while (reader.isStartTag())
		{
			String elementName = reader.getElementName();
			SpecifiedMember specifiedMember = members[index++];
			ITypeInfoItem specMember = specifiedMember.getSpecifiedMember();
			if (unspecifiedElement.equals(elementName))
			{
				reader.moveOverElementEnd();
				if (specMember != null)
				{
					specMember.setValue(obj, Boolean.FALSE);
				}
				continue;
			}
			if (specMember != null)
			{
				specMember.setValue(obj, Boolean.TRUE);
			}
			ITypeInfoItem member = specifiedMember.getMember();
			Object memberValue;
			if (primitiveElement.equals(elementName))
			{
				String value = reader.getAttributeValue(valueAttribute);
				memberValue = conversionHelper.convertValueToType(member.getRealType(), value);
				reader.moveOverElementEnd();
			}
			else
			{
				memberValue = reader.readObject(member.getRealType());
			}
			if (specifiedMember.getSpecifiedMember() != null)
			{
				specifiedMember.getSpecifiedMember().setValue(obj, Boolean.TRUE);
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
