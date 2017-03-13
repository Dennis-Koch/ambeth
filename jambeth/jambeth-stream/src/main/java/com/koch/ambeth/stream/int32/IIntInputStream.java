package com.koch.ambeth.stream.int32;

import com.koch.ambeth.stream.IInputStream;

public interface IIntInputStream extends IInputStream
{
	boolean hasInt();

	int readInt();
}