package de.osthus.ambeth.stream.strings;

import de.osthus.ambeth.stream.AbstractInputSourceConverter;
import de.osthus.ambeth.stream.AbstractInputSourceValueHolder;

public class StringInputSourceConverter extends AbstractInputSourceConverter
{
	@Override
	protected AbstractInputSourceValueHolder createValueHolderInstance()
	{
		return new StringInputSourceValueHolder();
	}
}
