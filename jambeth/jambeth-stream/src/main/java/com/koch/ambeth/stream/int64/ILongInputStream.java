package com.koch.ambeth.stream.int64;

import com.koch.ambeth.stream.IInputStream;

public interface ILongInputStream extends IInputStream
{
	boolean hasLong();

	long readLong();
}