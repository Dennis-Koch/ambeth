package com.koch.ambeth.persistence.jdbc.lob;

import java.io.IOException;

import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.stream.chars.ICharacterInputStream;

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
