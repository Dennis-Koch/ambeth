package de.osthus.ambeth.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public interface ICyclicXMLHandler
{
	String write(Object obj);

	void writeToStream(OutputStream outputStream, Object obj);

	void writeToChannel(WritableByteChannel byteChannel, Object obj);

	Object read(String cyclicXmlContent);

	Object readFromStream(InputStream inputStream);

	Object readFromChannel(ReadableByteChannel byteChannel);
}
