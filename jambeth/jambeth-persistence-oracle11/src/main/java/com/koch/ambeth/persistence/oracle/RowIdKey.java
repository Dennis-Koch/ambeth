package com.koch.ambeth.persistence.oracle;

import java.nio.charset.Charset;
import java.util.Arrays;

public class RowIdKey
{
	private static final Charset utf8 = Charset.forName("UTF-8");

	protected final byte[] value;

	public RowIdKey(byte[] value)
	{
		this.value = value;
	}

	public byte[] getValue()
	{
		return value;
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode(value);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof RowIdKey))
		{
			return false;
		}
		RowIdKey other = (RowIdKey) obj;
		return Arrays.equals(value, other.value);
	}

	@Override
	public String toString()
	{
		return new String(value, utf8);
	}
}
