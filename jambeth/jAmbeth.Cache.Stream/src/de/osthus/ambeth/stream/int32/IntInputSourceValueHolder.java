package de.osthus.ambeth.stream.int32;

import de.osthus.ambeth.stream.AbstractInputSourceValueHolder;
import de.osthus.ambeth.stream.IInputStream;

public class IntInputSourceValueHolder extends AbstractInputSourceValueHolder implements IIntInputSource
{
	@Override
	public IInputStream deriveInputStream()
	{
		return deriveIntInputStream();
	}

	@Override
	public IIntInputStream deriveIntInputStream()
	{
		return new BinaryToIntInputStream(createBinaryInputStream());
	}
}
