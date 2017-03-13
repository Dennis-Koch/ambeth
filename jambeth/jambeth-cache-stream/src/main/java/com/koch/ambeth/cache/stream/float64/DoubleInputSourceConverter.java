package com.koch.ambeth.cache.stream.float64;

import com.koch.ambeth.cache.stream.AbstractInputSourceConverter;
import com.koch.ambeth.cache.stream.AbstractInputSourceValueHolder;

public class DoubleInputSourceConverter extends AbstractInputSourceConverter
{
	@Override
	protected AbstractInputSourceValueHolder createValueHolderInstance()
	{
		return new DoubleInputSourceValueHolder();
	}
}
