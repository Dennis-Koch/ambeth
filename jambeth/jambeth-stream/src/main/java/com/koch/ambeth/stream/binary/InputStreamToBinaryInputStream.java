package com.koch.ambeth.stream.binary;

import java.io.IOException;
import java.io.InputStream;

import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class InputStreamToBinaryInputStream implements IBinaryInputStream, IInputStream
{
	protected final InputStream is;

	public InputStreamToBinaryInputStream(InputStream is)
	{
		this.is = is;
	}

	@Override
	public void close() throws IOException
	{
		is.close();
	}

	@Override
	public int readByte()
	{
		try
		{
			return is.read();
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
