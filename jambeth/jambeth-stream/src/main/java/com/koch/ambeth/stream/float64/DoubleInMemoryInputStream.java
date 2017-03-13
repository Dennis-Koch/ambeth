package com.koch.ambeth.stream.float64;

import java.io.IOException;

/**
 * Provides a binary stream of IEEE 754 bit-encoded double values. These explicitly means that every 8 bytes belong to the same double value
 */
public class DoubleInMemoryInputStream implements IDoubleInputStream
{
	public static final IDoubleInputStream EMPTY_INPUT_STREAM = new IDoubleInputStream()
	{
		@Override
		public void close() throws IOException
		{
			// Intended blank
		}

		@Override
		public double readDouble()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasDouble()
		{
			return false;
		}
	};

	private int index = -1;

	private final double[] array;

	public DoubleInMemoryInputStream(double[] array)
	{
		this.array = array;
	}

	@Override
	public void close() throws IOException
	{
		// Intended blank
	}

	@Override
	public boolean hasDouble()
	{
		return (array.length > index + 1);
	}

	@Override
	public double readDouble()
	{
		return array[++index];
	}
}
