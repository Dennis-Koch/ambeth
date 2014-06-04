package de.osthus.ambeth.stream.float32;

import java.io.IOException;
import java.io.InputStream;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public class BinaryToFloatInputStream implements IFloatInputStream
{
	private final InputStream is;

	private final byte[] input = new byte[4];

	private int inputOffset;

	public BinaryToFloatInputStream(InputStream is)
	{
		this.is = is;
	}

	@Override
	public void close() throws IOException
	{
		is.close();
	}

	@Override
	public boolean hasFloat()
	{
		if (inputOffset == 4)
		{
			return true;
		}
		int length;
		try
		{
			length = is.read(input, inputOffset, 4 - inputOffset);
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		inputOffset += length;
		return (inputOffset == 4);
	}

	@Override
	public float readFloat()
	{
		if (inputOffset != 4)
		{
			throw new IllegalStateException("Not allowed");
		}
		inputOffset = 0;
		byte[] input = this.input;
		// IEEE 754
		int intValue = (input[0] & 0xff) << (3 * 8);
		intValue += (input[1] & 0xff) << (2 * 8);
		intValue += (input[2] & 0xff) << (1 * 8);
		intValue += (input[3] & 0xff) << (0 * 8);
		return Float.intBitsToFloat(intValue);
	}
}