package com.koch.ambeth.stream.binary;

import java.io.IOException;
import java.io.InputStream;

public class BinaryInputStream extends InputStream
{
	private final IBinaryInputStream is;

	public BinaryInputStream(IBinaryInputStream is)
	{
		this.is = is;
	}

	@Override
	public void close() throws IOException
	{
		is.close();
	}

	@Override
	public int read() throws IOException
	{
		return is.readByte();
	}
}
