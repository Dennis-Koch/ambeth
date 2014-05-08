package de.osthus.ambeth.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class CyclicXMLHandler implements ICyclicXMLHandler, ICyclicXmlWriter, ICyclicXmlReader
{
	@LogInstance
	private ILogger log;

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
}
