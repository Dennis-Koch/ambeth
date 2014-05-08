package de.osthus.ambeth.stream.float32;

import de.osthus.ambeth.stream.AbstractInputSourceConverter;
import de.osthus.ambeth.stream.AbstractInputSourceValueHolder;

public class FloatInputSourceConverter extends AbstractInputSourceConverter
{
	@Override
	protected AbstractInputSourceValueHolder createValueHolderInstance()
	{
		return new FloatInputSourceValueHolder();
	}
}
