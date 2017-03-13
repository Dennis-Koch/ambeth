package com.koch.ambeth.cache.stream.bool;

import com.koch.ambeth.cache.stream.AbstractInputSourceConverter;
import com.koch.ambeth.cache.stream.AbstractInputSourceValueHolder;

public class BooleanInputSourceConverter extends AbstractInputSourceConverter
{
	@Override
	protected AbstractInputSourceValueHolder createValueHolderInstance()
	{
		return new BooleanInputSourceValueHolder();
	}
}
