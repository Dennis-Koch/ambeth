package com.koch.ambeth.util.io;

import java.io.IOException;
import java.io.OutputStream;

public class ConfigurableGZIPOutputStream extends java.util.zip.GZIPOutputStream
{
	public ConfigurableGZIPOutputStream(OutputStream out, int compressionLevel) throws IOException
	{
		super(out);
		setCompressionLevel(compressionLevel);
	}

	public ConfigurableGZIPOutputStream(OutputStream out, int compressionLevel, int size) throws IOException
	{
		super(out, size);
		setCompressionLevel(compressionLevel);
	}

	public void setCompressionLevel(int compressionLevel)
	{
		def.setLevel(compressionLevel);
	}
}
