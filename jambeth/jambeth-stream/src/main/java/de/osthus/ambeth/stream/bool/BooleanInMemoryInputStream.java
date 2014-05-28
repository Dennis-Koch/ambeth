package de.osthus.ambeth.stream.bool;

import java.io.IOException;

/**
 * Provides a binary stream of byte-encoded boolean values. This explicitly means that 7 bits remain unused.
 */
public class BooleanInMemoryInputStream implements IBooleanInputStream
{
	public static final IBooleanInputStream EMPTY_INPUT_STREAM = new IBooleanInputStream()
	{
		@Override
		public void close() throws IOException
		{
			// Intended blank
		}

		@Override
		public boolean readBoolean()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasBoolean()
		{
			return false;
		}
	};

	private int index = -1;

	private final boolean[] array;

	public BooleanInMemoryInputStream(boolean[] array)
	{
		this.array = array;
	}

	@Override
	public void close() throws IOException
	{
		// Intended blank
	}

	@Override
	public boolean hasBoolean()
	{
		return (array.length > index + 1);
	}

	@Override
	public boolean readBoolean()
	{
		return array[++index];
	}
}
