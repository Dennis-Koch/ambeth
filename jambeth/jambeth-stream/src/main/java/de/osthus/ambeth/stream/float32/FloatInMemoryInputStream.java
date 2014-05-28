package de.osthus.ambeth.stream.float32;

import java.io.IOException;

/**
 * Provides a binary stream IEEE 754 bit-encoded float values. These explicitly means that every 4 bytes belong to the same float value
 */
public class FloatInMemoryInputStream implements IFloatInputStream
{
	public static final IFloatInputStream EMPTY_INPUT_STREAM = new IFloatInputStream()
	{
		@Override
		public void close() throws IOException
		{
			// Intended blank
		}

		@Override
		public float readFloat()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasFloat()
		{
			return false;
		}
	};

	private int index = -1;

	private final float[] array;

	public FloatInMemoryInputStream(float[] array)
	{
		this.array = array;
	}

	@Override
	public void close() throws IOException
	{
		// Intended blank
	}

	@Override
	public boolean hasFloat()
	{
		return (array.length > index + 1);
	}

	@Override
	public float readFloat()
	{
		return array[++index];
	}
}
