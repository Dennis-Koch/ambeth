package com.koch.ambeth.stream.int64;

import java.io.IOException;

import com.koch.ambeth.stream.binary.IBinaryInputStream;

/**
 * Provides a binary stream bit-encoded long values. These explicitly means that every 8 bytes belong to the same long value
 */
public class LongToBinaryInputStream implements IBinaryInputStream
{
	private int outputIndex = 8;

	private final int[] output = new int[8];

	private final ILongInputStream is;

	public LongToBinaryInputStream(ILongInputStream is)
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
		if (outputIndex == 8)
		{
			if (!is.hasLong())
			{
				return -1;
			}
			outputIndex = 0;
			long longValue = is.readLong();
			output[0] = (int) ((longValue >> (7 * 8)) & 0xff);
			output[1] = (int) ((longValue >> (6 * 8)) & 0xff);
			output[2] = (int) ((longValue >> (5 * 8)) & 0xff);
			output[3] = (int) ((longValue >> (4 * 8)) & 0xff);
			output[4] = (int) ((longValue >> (3 * 8)) & 0xff);
			output[5] = (int) ((longValue >> (2 * 8)) & 0xff);
			output[6] = (int) ((longValue >> (1 * 8)) & 0xff);
			output[7] = (int) ((longValue >> (0 * 8)) & 0xff);
		}
		return output[outputIndex++];
	}
}
