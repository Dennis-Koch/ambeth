package com.koch.ambeth.cache.stream.strings;

import com.koch.ambeth.cache.stream.AbstractInputSourceConverter;
import com.koch.ambeth.cache.stream.AbstractInputSourceValueHolder;

public class StringInputSourceConverter extends AbstractInputSourceConverter
{
	@Override
	protected AbstractInputSourceValueHolder createValueHolderInstance()
	{
		return new StringInputSourceValueHolder();
	}
}
