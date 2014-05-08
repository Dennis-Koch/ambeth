package de.osthus.ambeth.xml;

import java.io.InputStream;
import java.io.Reader;
import java.nio.channels.ReadableByteChannel;

public interface ICyclicXmlReader
{
	Object read(String cyclicXmlContent);

	Object readFromStream(InputStream is);

	Object readFromStream(InputStream is, String encoding);

	Object readFromChannel(ReadableByteChannel byteChannel);

	Object readFromReader(Reader reader);
}