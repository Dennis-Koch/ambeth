package de.osthus.ambeth.stream.chars;

import java.io.IOException;
import java.io.InputStream;

public class CharacterInputStream extends InputStream
{
	private final ICharacterInputStream is;

	public CharacterInputStream(ICharacterInputStream is)
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
		return is.readChar();
	}
}
