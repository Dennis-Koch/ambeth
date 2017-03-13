package com.koch.ambeth.cache.stream.float32;

import com.koch.ambeth.cache.stream.AbstractInputSourceConverter;
import com.koch.ambeth.cache.stream.AbstractInputSourceValueHolder;

public class FloatInputSourceConverter extends AbstractInputSourceConverter
{
	@Override
	protected AbstractInputSourceValueHolder createValueHolderInstance()
	{
		return new FloatInputSourceValueHolder();
	}
}
