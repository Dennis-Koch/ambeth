package de.osthus.ambeth.stream.float32;

import de.osthus.ambeth.stream.AbstractInputSourceValueHolder;
import de.osthus.ambeth.stream.IInputStream;

public class FloatInputSourceValueHolder extends AbstractInputSourceValueHolder implements IFloatInputSource
{
	@Override
	public IInputStream deriveInputStream()
	{
		return deriveFloatInputStream();
	}

	@Override
	public IFloatInputStream deriveFloatInputStream()
	{
		return new BinaryToFloatInputStream(createBinaryInputStream());
	}
}
