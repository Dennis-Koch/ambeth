package com.koch.ambeth.cache.stream.int64;

import com.koch.ambeth.cache.stream.AbstractInputSourceValueHolder;
import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.stream.int64.BinaryToLongInputStream;
import com.koch.ambeth.stream.int64.ILongInputSource;
import com.koch.ambeth.stream.int64.ILongInputStream;

public class LongInputSourceValueHolder extends AbstractInputSourceValueHolder implements ILongInputSource
{
	@Override
	public IInputStream deriveInputStream()
	{
		return deriveLongInputStream();
	}

	@Override
	public ILongInputStream deriveLongInputStream()
	{
		return new BinaryToLongInputStream(createBinaryInputStream());
	}
}
