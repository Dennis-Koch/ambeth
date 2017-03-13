package com.koch.ambeth.stream.float64;

import com.koch.ambeth.stream.IInputStream;

public interface IDoubleInputStream extends IInputStream
{
	boolean hasDouble();

	double readDouble();
}