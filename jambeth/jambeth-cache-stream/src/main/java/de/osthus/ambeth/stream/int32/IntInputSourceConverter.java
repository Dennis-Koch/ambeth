package de.osthus.ambeth.stream.int32;

import de.osthus.ambeth.stream.AbstractInputSourceConverter;
import de.osthus.ambeth.stream.AbstractInputSourceValueHolder;

public class IntInputSourceConverter extends AbstractInputSourceConverter
{
	@Override
	protected AbstractInputSourceValueHolder createValueHolderInstance()
	{
		return new IntInputSourceValueHolder();
	}
}
