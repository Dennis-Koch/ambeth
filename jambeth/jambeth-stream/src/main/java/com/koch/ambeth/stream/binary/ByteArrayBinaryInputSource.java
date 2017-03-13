package com.koch.ambeth.stream.binary;

import java.io.ByteArrayInputStream;

import com.koch.ambeth.stream.IInputStream;

public class ByteArrayBinaryInputSource implements IBinaryInputSource
{
	protected final byte[] array;

	public ByteArrayBinaryInputSource(byte[] array)
	{
		this.array = array;
	}

	@Override
	public IInputStream deriveInputStream()
	{
		return createIInputStream();
	}

	@Override
	public InputStreamToBinaryInputStream deriveBinaryInputStream()
	{
		return createIInputStream();
	}

	private InputStreamToBinaryInputStream createIInputStream()
	{
		return new InputStreamToBinaryInputStream(new ByteArrayInputStream(array));
	}
}
