package de.osthus.ambeth.stream.bool;

import de.osthus.ambeth.stream.AbstractInputSourceValueHolder;
import de.osthus.ambeth.stream.IInputStream;

public class BooleanInputSourceValueHolder extends AbstractInputSourceValueHolder implements IBooleanInputSource
{
	@Override
	public IInputStream deriveInputStream()
	{
		return deriveBooleanInputStream();
	}

	@Override
	public IBooleanInputStream deriveBooleanInputStream()
	{
		return new BinaryToBooleanInputStream(createBinaryInputStream());
	}
}
