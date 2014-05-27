package de.osthus.ambeth.stream.float64;

import de.osthus.ambeth.stream.AbstractInputSourceValueHolder;
import de.osthus.ambeth.stream.IInputStream;

public class DoubleInputSourceValueHolder extends AbstractInputSourceValueHolder implements IDoubleInputSource
{
	@Override
	public IInputStream deriveInputStream()
	{
		return deriveDoubleInputStream();
	}

	@Override
	public IDoubleInputStream deriveDoubleInputStream()
	{
		return new BinaryToDoubleInputStream(createBinaryInputStream());
	}
}
