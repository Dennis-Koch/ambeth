package com.koch.ambeth.stream.strings;

import java.io.IOException;

import com.koch.ambeth.stream.binary.IBinaryInputStream;

/**
 * Provides a binary stream bit-encoded long values. These explicitly means that every 8 bytes belong to the same long value
 */
public class StringToBinaryInputStream implements IBinaryInputStream
{
	public static final int NULL_STRING_BYTE = 10;

	public static final int VALID_STRING_BYTE = 11;

	private static int HEADER_POS = -1, INIT_POS = -2;

	private int outputIndex = INIT_POS;

	private String output;

	private final IStringInputStream is;

	public StringToBinaryInputStream(IStringInputStream is)
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
		if (outputIndex == INIT_POS)
		{
			if (!is.hasString())
			{
				return -1;
			}
			outputIndex = HEADER_POS;
			output = is.readString();
		}
		if (output == null)
		{
			outputIndex = INIT_POS;
			return NULL_STRING_BYTE;
		}
		if (outputIndex == HEADER_POS)
		{
			outputIndex++;
			return VALID_STRING_BYTE;
		}
		char oneChar = output.charAt(outputIndex++);
		if (outputIndex == output.length())
		{
			outputIndex = INIT_POS;
		}
		return oneChar;
	}
}
