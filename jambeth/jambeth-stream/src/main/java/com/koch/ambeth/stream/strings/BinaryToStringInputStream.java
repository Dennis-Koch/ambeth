package com.koch.ambeth.stream.strings;

import java.io.IOException;
import java.io.InputStream;

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class BinaryToStringInputStream implements IStringInputStream
{
	private final byte[] buffer = new byte[64];

	private int bufferCurrIndex = -2;

	private int bufferEndIndex = bufferCurrIndex;

	private final InputStream is;

	private final StringBuilder sb = new StringBuilder();

	private String value;

	private boolean hasValue;

	private boolean sbActive;

	public BinaryToStringInputStream(InputStream is)
	{
		this.is = is;
	}

	@Override
	public void close() throws IOException
	{
		is.close();
	}

	@Override
	public boolean hasString()
	{
		while (!hasValue)
		{
			if (bufferEndIndex == -1)
			{
				return false;
			}
			if (bufferCurrIndex == bufferEndIndex)
			{
				int length;
				try
				{
					length = is.read(buffer, 0, buffer.length);
				}
				catch (IOException e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
				bufferCurrIndex = 0;
				bufferEndIndex = length;
			}
			while (bufferCurrIndex < bufferEndIndex)
			{
				byte oneByte = buffer[bufferCurrIndex];
				if (!sbActive)
				{
					if (oneByte == StringToBinaryInputStream.NULL_STRING_BYTE)
					{
						value = null;
						hasValue = true;
						bufferCurrIndex++;
						break;
					}
					else if (oneByte == StringToBinaryInputStream.VALID_STRING_BYTE)
					{
						sbActive = true;
						bufferCurrIndex++;
						continue;
					}
					else
					{
						throw new IllegalStateException("Illegal byte");
					}
				}
				if (oneByte == StringToBinaryInputStream.NULL_STRING_BYTE || oneByte == StringToBinaryInputStream.VALID_STRING_BYTE)
				{
					value = sb.length() > 0 ? sb.toString() : "";
					hasValue = true;
					sb.setLength(0);
					sbActive = false;
					// Do NOT move the buffer index, because we did not yet consume the current header but instead finished the previously read valid
					// string
					break;
				}
				sb.append((char) oneByte);
				bufferCurrIndex++;
			}
		}
		return hasValue;
	}

	@Override
	public String readString()
	{
		if (!hasValue)
		{
			throw new IllegalStateException("Not allowed");
		}
		hasValue = false;
		return value;
	}
}
