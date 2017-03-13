package com.koch.ambeth.cache.stream.bool;

import com.koch.ambeth.cache.stream.AbstractInputSourceValueHolder;
import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.stream.bool.BinaryToBooleanInputStream;
import com.koch.ambeth.stream.bool.IBooleanInputSource;
import com.koch.ambeth.stream.bool.IBooleanInputStream;

public class BooleanInputSourceValueHolder extends AbstractInputSourceValueHolder implements IBooleanInputSource
{
	@Override
	public IInputStream deriveInputStream()
	{
		return deriveBooleanInputStream();
	}

	@Override
	public IBooleanInputStream deriveBooleanInputStream()
	{
		return new BinaryToBooleanInputStream(createBinaryInputStream());
	}
}
