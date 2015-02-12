package de.osthus.ambeth.stream.chars;

import java.io.IOException;
import java.io.Reader;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.stream.IInputStream;

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
