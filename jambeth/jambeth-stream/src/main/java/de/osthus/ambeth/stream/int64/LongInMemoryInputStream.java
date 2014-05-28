package de.osthus.ambeth.stream.int64;

import java.io.IOException;

/**
 * Provides a binary stream bit-encoded long values. These explicitly means that every 8 bytes belong to the same long value
 */
public class LongInMemoryInputStream implements ILongInputStream
{
	public static final ILongInputStream EMPTY_INPUT_STREAM = new ILongInputStream()
	{
		@Override
		public void close() throws IOException
		{
			// Intended blank
		}

		@Override
		public long readLong()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasLong()
		{
			return false;
		}
	};

	private int index = -1;

	private final long[] array;

	public LongInMemoryInputStream(long[] array)
	{
		this.array = array;
	}

	@Override
	public void close() throws IOException
	{
		// Intended blank
	}

	@Override
	public boolean hasLong()
	{
		return (array.length > index + 1);
	}

	@Override
	public long readLong()
	{
		return array[++index];
	}
}
