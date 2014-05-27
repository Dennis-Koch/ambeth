package de.osthus.ambeth.xml.simple;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.extendable.ClassExtendableContainer;
import de.osthus.ambeth.ioc.extendable.MapExtendableContainer;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.xml.ICyclicXmlController;
import de.osthus.ambeth.xml.INameBasedHandler;
import de.osthus.ambeth.xml.INameBasedHandlerExtendable;
import de.osthus.ambeth.xml.IReader;
import de.osthus.ambeth.xml.ITypeBasedHandler;
import de.osthus.ambeth.xml.ITypeBasedHandlerExtendable;
import de.osthus.ambeth.xml.IWriter;

public class SimpleXmlController implements ICyclicXmlController, ITypeBasedHandlerExtendable, INameBasedHandlerExtendable
{
	protected final ClassExtendableContainer<ITypeBasedHandler> typeToElementHandlers = new ClassExtendableContainer<ITypeBasedHandler>("elementHandler",
			"type");

	protected final MapExtendableContainer<String, INameBasedHandler> nameToElementHandlers = new MapExtendableContainer<String, INameBasedHandler>(
			"elementHandler", "name");

	@Autowired
	protected IProxyHelper proxyHelper;

	@Override
	public Object readObject(IReader reader)
	{
		return readObject(Object.class, reader);
	}

	@Override
	public Object readObject(Class<?> returnType, IReader reader)
	{
		String elementName = reader.getElementName();
		INameBasedHandler nameBasedHandler = nameToElementHandlers.getExtension(elementName);
		if (nameBasedHandler == null)
		{
			throw new IllegalArgumentException("Could not read object: " + elementName);
		}
		return nameBasedHandler.readObject(returnType, elementName, 0, reader);
	}

	@Override
	public void writeObject(Object obj, IWriter writer)
	{
		Class<?> type = proxyHelper.getRealType(obj.getClass());
		ITypeBasedHandler extension = typeToElementHandlers.getExtension(type);
		if (extension == null)
		{
			throw new IllegalArgumentException("Could not write object: " + obj);
		}
		extension.writeObject(obj, type, writer);
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
		nameToElementHandlers.register(nameBasedElementHandler, elementName);
	}

	@Override
	public void unregisterNameBasedElementHandler(INameBasedHandler nameBasedElementHandler, String elementName)
	{
		nameToElementHandlers.unregister(nameBasedElementHandler, elementName);
	}
}
