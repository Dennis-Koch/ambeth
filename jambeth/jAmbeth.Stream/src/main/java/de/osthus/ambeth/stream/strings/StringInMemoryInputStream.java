package de.osthus.ambeth.stream.strings;

import java.io.IOException;

/**
 * Provides a binary stream bit-encoded long values. These explicitly means that every 8 bytes belong to the same long value
 */
public class StringInMemoryInputStream implements IStringInputStream
{
	public static final IStringInputStream EMPTY_INPUT_STREAM = new IStringInputStream()
	{
		@Override
		public void close() throws IOException
		{
			// Intended blank
		}

		@Override
		public String readString()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasString()
		{
			return false;
		}
	};

	private int index = -1;

	private final String[] array;

	public StringInMemoryInputStream(String[] array)
	{
		this.array = array;
	}

	@Override
	public void close() throws IOException
	{
		// Intended blank
	}

	@Override
	public boolean hasString()
	{
		return (array.length > index + 1);
	}

	@Override
	public String readString()
	{
		return array[++index];
	}
}
