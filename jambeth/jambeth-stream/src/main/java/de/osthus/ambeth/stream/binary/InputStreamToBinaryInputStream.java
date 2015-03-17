package de.osthus.ambeth.stream.binary;

import java.io.IOException;
import java.io.InputStream;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.stream.IInputStream;

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
