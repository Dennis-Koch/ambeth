package com.koch.ambeth.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

public class CyclicXmlHandler implements ICyclicXMLHandler, ICyclicXmlWriter, ICyclicXmlReader, ITypeBasedHandlerExtendable, INameBasedHandlerExtendable
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected INameBasedHandlerExtendable nameBasedHandlerExtendable;

	@Autowired
	protected ITypeBasedHandlerExtendable typeBasedHandlerExtendable;

	@Autowired
	protected ICyclicXmlReader cyclicXmlReader;

	@Autowired
	protected ICyclicXmlWriter cyclicXmlWriter;

	@Override
	public String write(Object obj)
	{
		return cyclicXmlWriter.write(obj);
	}

	@Override
	public void writeToStream(OutputStream outputStream, Object object)
	{
		cyclicXmlWriter.writeToStream(outputStream, object);
	}

	@Override
	public void writeToChannel(WritableByteChannel byteChannel, Object object)
	{
		cyclicXmlWriter.writeToChannel(byteChannel, object);
	}

	@Override
	public Object readFromReader(Reader reader)
	{
		return cyclicXmlReader.readFromReader(reader);
	}

	@Override
	public Object read(String cyclicXmlContent)
	{
		return cyclicXmlReader.read(cyclicXmlContent);
	}

	@Override
	public Object readFromStream(InputStream is)
	{
		return cyclicXmlReader.readFromStream(is);
	}

	@Override
	public Object readFromStream(InputStream is, String encoding)
	{
		return cyclicXmlReader.readFromStream(is, encoding);
	}

	@Override
	public Object readFromChannel(ReadableByteChannel byteChannel)
	{
		return cyclicXmlReader.readFromChannel(byteChannel);
	}

	@Override
	public void registerElementHandler(ITypeBasedHandler elementHandler, Class<?> type)
	{
		typeBasedHandlerExtendable.registerElementHandler(elementHandler, type);
	}

	@Override
	public void unregisterElementHandler(ITypeBasedHandler elementHandler, Class<?> type)
	{
		typeBasedHandlerExtendable.unregisterElementHandler(elementHandler, type);
	}

	@Override
	public void registerNameBasedElementHandler(INameBasedHandler nameBasedElementHandler, String elementName)
	{
		nameBasedHandlerExtendable.registerNameBasedElementHandler(nameBasedElementHandler, elementName);
	}

	@Override
	public void unregisterNameBasedElementHandler(INameBasedHandler nameBasedElementHandler, String elementName)
	{
		nameBasedHandlerExtendable.unregisterNameBasedElementHandler(nameBasedElementHandler, elementName);
	}
}
