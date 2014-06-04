package de.osthus.ambeth.xml;

import java.util.List;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IntKeyMap;
import de.osthus.ambeth.ioc.extendable.ClassExtendableContainer;
import de.osthus.ambeth.ioc.extendable.MapExtendableContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.xml.typehandler.AbstractHandler;

public class CyclicXmlController extends AbstractHandler implements ICyclicXmlController, ITypeBasedHandlerExtendable, INameBasedHandlerExtendable
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IProxyHelper proxyHelper;

	protected IXmlTypeRegistry xmlTypeRegistry;

	protected final ClassExtendableContainer<ITypeBasedHandler> typeToElementHandlers = new ClassExtendableContainer<ITypeBasedHandler>("elementHandler",
			"type");

	protected final MapExtendableContainer<String, INameBasedHandler> nameBasedElementReaders = new MapExtendableContainer<String, INameBasedHandler>(
			"nameBasedElementReader", "elementName");

	protected final List<INameBasedHandler> nameBasedElementWriters = new ArrayList<INameBasedHandler>();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(proxyHelper, "proxyHelper");
		ParamChecker.assertNotNull(xmlTypeRegistry, "xmlTypeRegistry");
	}

	public void setProxyHelper(IProxyHelper proxyHelper)
	{
		this.proxyHelper = proxyHelper;
	}

	public void setXmlTypeRegistry(IXmlTypeRegistry xmlTypeRegistry)
	{
		this.xmlTypeRegistry = xmlTypeRegistry;
	}

	@Override
	public void registerElementHandler(ITypeBasedHandler elementHandler, Class<?> type)
	{
		typeToElementHandlers.register(elementHandler, type);
	}

	@Override
	public void unregisterElementHandler(ITypeBasedHandler elementHandler, Class<?> type)
	{
		typeToElementHandlers.unregister(elementHandler, type);
	}

	@Override
	public void registerNameBasedElementHandler(INameBasedHandler nameBasedElementHandler, String elementName)
	{
		nameBasedElementReaders.register(nameBasedElementHandler, elementName);
		nameBasedElementWriters.add(nameBasedElementHandler);
	}

	@Override
	public void unregisterNameBasedElementHandler(INameBasedHandler nameBasedElementHandler, String elementName)
	{
		nameBasedElementReaders.unregister(nameBasedElementHandler, elementName);
		nameBasedElementWriters.remove(nameBasedElementHandler);
	}

	@Override
	public Object readObject(IReader reader)
	{
		return readObject(Object.class, reader);
	}

	@Override
	public Object readObject(Class<?> returnType, IReader reader)
	{
		String elementName = reader.getElementName();
		if (xmlDictionary.getNullElement().equals(elementName))
		{
			reader.moveOverElementEnd();
			return null;
		}
		String idValue = reader.getAttributeValue(xmlDictionary.getIdAttribute());
		int id = idValue != null && idValue.length() > 0 ? Integer.parseInt(idValue) : 0;
		if (xmlDictionary.getRefElement().equals(elementName))
		{
			reader.moveOverElementEnd();
			return reader.getObjectById(id);
		}
		Object obj;
		if (xmlDictionary.getObjectElement().equals(elementName))
		{
			Class<?> type = classElementHandler.readFromAttribute(reader);
			obj = readObjectContent(returnType, type, id, reader);
		}
		else
		{
			INameBasedHandler nameBasedElementReader = nameBasedElementReaders.getExtension(elementName);
			if (nameBasedElementReader == null)
			{
				throw new IllegalStateException("Element name '" + elementName + "' not supported");
			}
			obj = nameBasedElementReader.readObject(returnType, elementName, id, reader);
		}
		if (id > 0)
		{
			reader.putObjectWithId(obj, id);
		}
		reader.moveOverElementEnd();
		return obj;
	}

	@Override
	public void writeObject(Object obj, IWriter writer)
	{
		if (obj == null)
		{
			writer.writeStartElement(xmlDictionary.getNullElement());
			writer.writeEndElement();
			return;
		}
		Integer idValue = writer.getIdOfObject(obj);
		if (idValue > 0)
		{
			writer.writeStartElement(xmlDictionary.getRefElement());
			writer.writeAttribute(xmlDictionary.getIdAttribute(), idValue.toString());
			writer.writeEndElement();
			return;
		}
		Class<?> type = proxyHelper.getRealType(obj.getClass());
		for (int a = 0, size = nameBasedElementWriters.size(); a < size; a++)
		{
			INameBasedHandler nameBasedElementWriter = nameBasedElementWriters.get(a);
			if (nameBasedElementWriter.writesCustom(obj, type, writer))
			{
				return;
			}
		}
		int id = writer.acquireIdForObject(obj);
		String objectElement = xmlDictionary.getObjectElement();
		writer.writeStartElement(objectElement);
		if (id > 0)
		{
			writer.writeAttribute(xmlDictionary.getIdAttribute(), Integer.toString(id));
		}
		classElementHandler.writeAsAttribute(type, writer);
		writeObjectContent(obj, type, writer);
		writer.writeCloseElement(objectElement);
	}

	protected Object readObjectContent(Class<?> returnType, Class<?> type, int id, IReader reader)
	{
		ITypeBasedHandler extension = typeToElementHandlers.getExtension(type);
		if (extension == null)
		{
			throw new IllegalStateException("No extension mapped to type '" + type.getName() + "' found");
		}
		return extension.readObject(returnType, type, id, reader);
	}

	protected void writeObjectContent(Object obj, Class<?> type, IWriter writer)
	{
		ITypeBasedHandler extension = typeToElementHandlers.getExtension(type);
		if (extension == null)
		{
			throw new IllegalStateException("No extension mapped to type '" + type.getName() + "' found");
		}
		extension.writeObject(obj, type, writer);
	}

	protected Object resolveObjectById(IntKeyMap<Object> idToObjectMap, int id)
	{
		Object obj = idToObjectMap.get(id);
		if (obj == null)
		{
			throw new IllegalStateException("No object found with id " + id);
		}
		return obj;
	}
}