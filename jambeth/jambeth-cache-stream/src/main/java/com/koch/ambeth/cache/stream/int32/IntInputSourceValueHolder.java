package com.koch.ambeth.cache.stream.int32;

import com.koch.ambeth.cache.stream.AbstractInputSourceValueHolder;
import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.stream.int32.BinaryToIntInputStream;
import com.koch.ambeth.stream.int32.IIntInputSource;
import com.koch.ambeth.stream.int32.IIntInputStream;

public class IntInputSourceValueHolder extends AbstractInputSourceValueHolder implements IIntInputSource
{
	@Override
	public IInputStream deriveInputStream()
	{
		return deriveIntInputStream();
	}

	@Override
	public IIntInputStream deriveIntInputStream()
	{
		return new BinaryToIntInputStream(createBinaryInputStream());
	}
}
