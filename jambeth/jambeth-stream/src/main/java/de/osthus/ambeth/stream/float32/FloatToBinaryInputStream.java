package de.osthus.ambeth.stream.float32;

import java.io.IOException;

import de.osthus.ambeth.stream.binary.IBinaryInputStream;

/**
 * Provides a binary stream IEEE 754 bit-encoded float values. These explicitly means that every 4 bytes belong to the same float value
 */
public class FloatToBinaryInputStream implements IBinaryInputStream
{
	private int outputIndex = 4;

	private final int[] output = new int[4];

	private final IFloatInputStream is;

	public FloatToBinaryInputStream(IFloatInputStream is)
	{
		this.is = is;
	}

	@Override
	public void close() throws IOException
	{
		is.close();
	}

	@Override
	public int readByte()
	{
		if (outputIndex == 4)
		{
			if (!is.hasFloat())
			{
				return -1;
			}
			outputIndex = 0;
			// IEEE 754
			float floatValue = is.readFloat();
			int intValue = Float.floatToIntBits(floatValue);
			output[0] = (intValue >> (3 * 8)) & 0xff;
			output[1] = (intValue >> (2 * 8)) & 0xff;
			output[2] = (intValue >> (1 * 8)) & 0xff;
			output[3] = (intValue >> (0 * 8)) & 0xff;
		}
		return output[outputIndex++];
	}
}
