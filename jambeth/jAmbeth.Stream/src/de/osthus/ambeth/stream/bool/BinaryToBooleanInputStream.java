package de.osthus.ambeth.stream.bool;

import java.io.IOException;
import java.io.InputStream;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public class BinaryToBooleanInputStream implements IBooleanInputStream
{
	private final InputStream is;

	private boolean inputValue;

	private boolean hasInput;

	public BinaryToBooleanInputStream(InputStream is)
	{
		this.is = is;
	}

	@Override
	public void close() throws IOException
	{
		is.close();
	}

	@Override
	public boolean hasBoolean()
	{
		if (!hasInput)
		{
			int oneByte;
			try
			{
				oneByte = is.read();
			}
			catch (IOException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			if (oneByte == -1)
			{
				return false;
			}
			inputValue = oneByte != 0;
			hasInput = true;
			return true;
		}
		return hasInput;
	}

	@Override
	public boolean readBoolean()
	{
		if (!hasInput)
		{
			throw new IllegalStateException("Not allowed");
		}
		hasInput = false;
		return inputValue;
	}
}
