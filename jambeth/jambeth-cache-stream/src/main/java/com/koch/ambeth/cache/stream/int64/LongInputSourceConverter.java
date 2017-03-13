package com.koch.ambeth.cache.stream.int64;

import com.koch.ambeth.cache.stream.AbstractInputSourceConverter;
import com.koch.ambeth.cache.stream.AbstractInputSourceValueHolder;

public class LongInputSourceConverter extends AbstractInputSourceConverter
{
	@Override
	protected AbstractInputSourceValueHolder createValueHolderInstance()
	{
		return new LongInputSourceValueHolder();
	}
}
