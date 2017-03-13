package com.koch.ambeth.xml.namehandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.xml.INameBasedHandler;
import com.koch.ambeth.xml.IReader;
import com.koch.ambeth.xml.IWriter;
import com.koch.ambeth.xml.IXmlTypeRegistry;
import com.koch.ambeth.xml.pending.ICommandBuilder;
import com.koch.ambeth.xml.pending.ICommandTypeRegistry;
import com.koch.ambeth.xml.pending.IObjectCommand;
import com.koch.ambeth.xml.pending.IObjectFuture;
import com.koch.ambeth.xml.typehandler.AbstractHandler;

public class CollectionElementHandler extends AbstractHandler implements INameBasedHandler, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected ICommandBuilder commandBuilder;

	protected IXmlTypeRegistry xmlTypeRegistry;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(commandBuilder, "commandBuilder");
		ParamChecker.assertNotNull(xmlTypeRegistry, "xmlTypeRegistry");
	}

	public void setCommandBuilder(ICommandBuilder commandBuilder)
	{
		this.commandBuilder = commandBuilder;
	}

	public void setXmlTypeRegistry(IXmlTypeRegistry xmlTypeRegistry)
	{
		this.xmlTypeRegistry = xmlTypeRegistry;
	}

	protected Class<?> getComponentTypeOfCollection(Object obj)
	{
		return Object.class;
	}

	@Override
	public boolean writesCustom(Object obj, Class<?> type, IWriter writer)
	{
		if (!Collection.class.isAssignableFrom(type))
		{
			return false;
		}
		Collection<?> coll = (Collection<?>) obj;
		String collElement;
		if (Set.class.isAssignableFrom(type))
		{
			collElement = xmlDictionary.getSetElement();
		}
		else if (List.class.isAssignableFrom(type))
		{
			collElement = xmlDictionary.getListElement();
		}
		else
		{
			throw new IllegalStateException("Collection of type " + type.getName() + " not supported");
		}
		writer.writeStartElement(collElement);
		int id = writer.acquireIdForObject(obj);
		writer.writeAttribute(xmlDictionary.getIdAttribute(), Integer.toString(id));
		int length = coll.size();
		writer.writeAttribute(xmlDictionary.getSizeAttribute(), Integer.toString(length));

		Class<?> componentType = getComponentTypeOfCollection(obj);
		classElementHandler.writeAsAttribute(componentType, writer);
		writer.writeStartElementEnd();
		Iterator<?> iter = coll.iterator();
		while (iter.hasNext())
		{
			Object item = iter.next();
			writer.writeObject(item);
		}
		writer.writeCloseElement(collElement);
		return true;
	}

	@Override
	public Object readObject(Class<?> returnType, String elementName, int id, IReader reader)
	{
		if (!xmlDictionary.getSetElement().equals(elementName) && !xmlDictionary.getListElement().equals(elementName))
		{
			throw new IllegalStateException("Element '" + elementName + "' not supported");
		}
		String lengthValue = reader.getAttributeValue(xmlDictionary.getSizeAttribute());
		int length = lengthValue != null && lengthValue.length() > 0 ? Integer.parseInt(lengthValue) : 0;

		// Read componentType because of typeId registration. This is intended to remain unused in java
		@SuppressWarnings("unused")
		Class<?> componentType = classElementHandler.readFromAttribute(reader);

		Collection<Object> coll;
		if (xmlDictionary.getSetElement().equals(elementName))
		{
			coll = length > 0 ? new HashSet<Object>((int) (length / 0.75f + 1), 0.75f) : new HashSet<Object>();
		}
		else
		{
			coll = length > 0 ? new ArrayList<Object>(length) : new ArrayList<Object>();
		}
		reader.putObjectWithId(coll, id);
		reader.nextTag();
		boolean useObjectFuture = false;
		ICommandBuilder commandBuilder = this.commandBuilder;
		ICommandTypeRegistry commandTypeRegistry = reader.getCommandTypeRegistry();
		while (reader.isStartTag())
		{
			Object item = reader.readObject();
			if (item instanceof IObjectFuture)
			{
				IObjectFuture objectFuture = (IObjectFuture) item;
				IObjectCommand command = commandBuilder.build(commandTypeRegistry, objectFuture, coll);
				reader.addObjectCommand(command);
				useObjectFuture = true;
			}
			else if (useObjectFuture)
			{
				IObjectCommand command = commandBuilder.build(commandTypeRegistry, null, coll, item);
				reader.addObjectCommand(command);
			}
			else
			{
				coll.add(item);
			}
		}
		return coll;
	}
}
