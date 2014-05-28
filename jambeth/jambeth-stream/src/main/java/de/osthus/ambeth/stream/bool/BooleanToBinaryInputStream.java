package de.osthus.ambeth.stream.bool;

import java.io.IOException;

import de.osthus.ambeth.stream.binary.IBinaryInputStream;

/**
 * Provides a binary stream of byte-encoded boolean values. This explicitly means that 7 bits remain unused.
 */
public class BooleanToBinaryInputStream implements IBinaryInputStream
{
	private final IBooleanInputStream is;

	public BooleanToBinaryInputStream(IBooleanInputStream is)
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
		if (!is.hasBoolean())
		{
			return -1;
		}
		return is.readBoolean() ? 1 : 0;
	}
}
