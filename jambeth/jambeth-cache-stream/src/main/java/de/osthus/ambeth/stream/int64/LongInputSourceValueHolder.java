package de.osthus.ambeth.stream.int64;

import de.osthus.ambeth.stream.AbstractInputSourceValueHolder;
import de.osthus.ambeth.stream.IInputStream;

public class LongInputSourceValueHolder extends AbstractInputSourceValueHolder implements ILongInputSource
{
	@Override
	public IInputStream deriveInputStream()
	{
		return deriveLongInputStream();
	}

	@Override
	public ILongInputStream deriveLongInputStream()
	{
		return new BinaryToLongInputStream(createBinaryInputStream());
	}
}
