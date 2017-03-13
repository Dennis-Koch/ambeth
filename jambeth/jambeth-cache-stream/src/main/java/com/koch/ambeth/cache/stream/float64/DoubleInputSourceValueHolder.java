package com.koch.ambeth.cache.stream.float64;

import com.koch.ambeth.cache.stream.AbstractInputSourceValueHolder;
import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.stream.float64.BinaryToDoubleInputStream;
import com.koch.ambeth.stream.float64.IDoubleInputSource;
import com.koch.ambeth.stream.float64.IDoubleInputStream;

public class DoubleInputSourceValueHolder extends AbstractInputSourceValueHolder implements IDoubleInputSource
{
	@Override
	public IInputStream deriveInputStream()
	{
		return deriveDoubleInputStream();
	}

	@Override
	public IDoubleInputStream deriveDoubleInputStream()
	{
		return new BinaryToDoubleInputStream(createBinaryInputStream());
	}
}
