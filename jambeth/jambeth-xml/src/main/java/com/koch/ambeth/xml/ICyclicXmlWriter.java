package com.koch.ambeth.xml;

import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;

public interface ICyclicXmlWriter
{
	String write(Object object);

	void writeToStream(OutputStream outputStream, Object object);

	void writeToChannel(WritableByteChannel byteChannel, Object object);
}