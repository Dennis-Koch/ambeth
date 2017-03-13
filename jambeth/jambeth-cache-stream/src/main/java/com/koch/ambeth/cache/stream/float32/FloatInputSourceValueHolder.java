package com.koch.ambeth.cache.stream.float32;

import com.koch.ambeth.cache.stream.AbstractInputSourceValueHolder;
import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.stream.float32.BinaryToFloatInputStream;
import com.koch.ambeth.stream.float32.IFloatInputSource;
import com.koch.ambeth.stream.float32.IFloatInputStream;

public class FloatInputSourceValueHolder extends AbstractInputSourceValueHolder implements IFloatInputSource
{
	@Override
	public IInputStream deriveInputStream()
	{
		return deriveFloatInputStream();
	}

	@Override
	public IFloatInputStream deriveFloatInputStream()
	{
		return new BinaryToFloatInputStream(createBinaryInputStream());
	}
}
