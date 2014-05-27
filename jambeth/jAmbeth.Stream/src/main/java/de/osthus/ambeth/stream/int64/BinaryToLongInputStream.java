package de.osthus.ambeth.stream.int64;

import java.io.IOException;
import java.io.InputStream;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public class BinaryToLongInputStream implements ILongInputStream
{
	private final InputStream is;

	private final byte[] input = new byte[8];

	private int inputOffset;

	public BinaryToLongInputStream(InputStream is)
	{
		this.is = is;
	}

	@Override
	public void close() throws IOException
	{
		is.close();
	}

	@Override
	public boolean hasLong()
	{
		if (inputOffset == 8)
		{
			return true;
		}
		int length;
		try
		{
			length = is.read(input, inputOffset, 8 - inputOffset);
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		inputOffset += length;
		return (inputOffset == 8);
	}

	@Override
	public long readLong()
	{
		if (inputOffset != 8)
		{
			throw new IllegalStateException("Not allowed");
		}
		inputOffset = 0;
		byte[] input = this.input;
		// IEEE 754
		long longValue = ((long) input[0] & 0xff) << (7 * 8);
		longValue += ((long) input[1] & 0xff) << (6 * 8);
		longValue += ((long) input[2] & 0xff) << (5 * 8);
		longValue += ((long) input[3] & 0xff) << (4 * 8);
		longValue += ((long) input[4] & 0xff) << (3 * 8);
		longValue += ((long) input[5] & 0xff) << (2 * 8);
		longValue += ((long) input[6] & 0xff) << (1 * 8);
		longValue += ((long) input[7] & 0xff) << (0 * 8);
		return longValue;
	}
}
