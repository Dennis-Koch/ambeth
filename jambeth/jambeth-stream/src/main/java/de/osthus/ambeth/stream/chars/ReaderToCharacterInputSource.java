package de.osthus.ambeth.stream.chars;

import java.io.Reader;

import de.osthus.ambeth.stream.IInputStream;

public class ReaderToCharacterInputSource implements ICharacterInputSource
{
	protected final Reader reader;

	public ReaderToCharacterInputSource(Reader reader)
	{
		this.reader = reader;
	}

	@Override
	public IInputStream deriveInputStream()
	{
		return new ReaderToCharacterInputStream(reader);
	}

	@Override
	public ICharacterInputStream deriveCharacterInputStream()
	{
		return new ReaderToCharacterInputStream(reader);
	}
}
