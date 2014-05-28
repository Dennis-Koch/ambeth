package de.osthus.ambeth.stream.float64;

import de.osthus.ambeth.stream.AbstractInputSourceConverter;
import de.osthus.ambeth.stream.AbstractInputSourceValueHolder;

public class DoubleInputSourceConverter extends AbstractInputSourceConverter
{
	@Override
	protected AbstractInputSourceValueHolder createValueHolderInstance()
	{
		return new DoubleInputSourceValueHolder();
	}
}
