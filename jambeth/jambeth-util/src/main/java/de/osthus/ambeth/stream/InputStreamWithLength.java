package de.osthus.ambeth.stream;

import java.io.InputStream;

public class InputStreamWithLength implements IInputStreamWithLength
{
	private final InputStream inputStream;

	private final int overallLength;

	public InputStreamWithLength(InputStream inputStream, int overallLength)
	{
		super();
		this.inputStream = inputStream;
		this.overallLength = overallLength;
	}

	@Override
	public InputStream getInputStream()
	{
		return inputStream;
	}

	@Override
	public int getOverallLength()
	{
		return overallLength;
	}

	@Override
	public String toString()
	{
		return super.toString() + ", Length: " + overallLength;
	}
}
