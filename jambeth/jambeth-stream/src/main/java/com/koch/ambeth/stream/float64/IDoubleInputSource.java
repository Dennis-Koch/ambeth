package com.koch.ambeth.stream.float64;

import com.koch.ambeth.stream.IInputSource;

public interface IDoubleInputSource extends IInputSource
{
	IDoubleInputStream deriveDoubleInputStream();
}
