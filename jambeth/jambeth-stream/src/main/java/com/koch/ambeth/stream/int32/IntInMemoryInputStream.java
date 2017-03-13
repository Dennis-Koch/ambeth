package com.koch.ambeth.stream.int32;

import java.io.IOException;

/**
 * Provides a binary stream IEEE 754 bit-encoded float values. These explicitly means that every 4 bytes belong to the same float value
 */
public class IntInMemoryInputStream implements IIntInputStream
{
	public static final IIntInputStream EMPTY_INPUT_STREAM = new IIntInputStream()
	{
		@Override
		public void close() throws IOException
		{
			// Intended blank
		}

		@Override
		public int readInt()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasInt()
		{
			return false;
		}
	};

	private int index = -1;

	private final int[] array;

	public IntInMemoryInputStream(int[] array)
	{
		this.array = array;
	}

	@Override
	public void close() throws IOException
	{
		// Intended blank
	}

	@Override
	public boolean hasInt()
	{
		return (array.length > index + 1);
	}

	@Override
	public int readInt()
	{
		return array[++index];
	}
}
