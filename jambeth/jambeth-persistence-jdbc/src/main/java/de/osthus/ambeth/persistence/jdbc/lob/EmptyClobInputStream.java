package de.osthus.ambeth.persistence.jdbc.lob;

import java.io.IOException;

import de.osthus.ambeth.stream.IInputStream;
import de.osthus.ambeth.stream.chars.ICharacterInputStream;

public class EmptyClobInputStream implements ICharacterInputStream, IInputStream
{
	@Override
	public int readChar()
	{
		return -1;
	}

	@Override
	public void close() throws IOException
	{
		// Intended blank
	}
}
