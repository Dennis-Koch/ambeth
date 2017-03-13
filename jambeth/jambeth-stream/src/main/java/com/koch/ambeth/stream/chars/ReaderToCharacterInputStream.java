package com.koch.ambeth.stream.chars;

import java.io.IOException;
import java.io.Reader;

import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class ReaderToCharacterInputStream implements ICharacterInputStream, IInputStream
{
	protected final Reader is;

	public ReaderToCharacterInputStream(Reader is)
	{
		this.is = is;
	}

	@Override
	public void close() throws IOException
	{
		is.close();
	}

	@Override
	public int readChar()
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
