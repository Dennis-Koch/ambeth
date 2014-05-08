package de.osthus.ambeth.stream.int64;

import de.osthus.ambeth.stream.AbstractInputSourceConverter;
import de.osthus.ambeth.stream.AbstractInputSourceValueHolder;

public class LongInputSourceConverter extends AbstractInputSourceConverter
{
	@Override
	protected AbstractInputSourceValueHolder createValueHolderInstance()
	{
		return new LongInputSourceValueHolder();
	}
}
