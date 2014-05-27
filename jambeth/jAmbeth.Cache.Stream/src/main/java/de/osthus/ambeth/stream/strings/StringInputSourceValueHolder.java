package de.osthus.ambeth.stream.strings;

import de.osthus.ambeth.stream.AbstractInputSourceValueHolder;
import de.osthus.ambeth.stream.IInputStream;

public class StringInputSourceValueHolder extends AbstractInputSourceValueHolder implements IStringInputSource
{
	@Override
	public IInputStream deriveInputStream()
	{
		return deriveStringInputStream();
	}

	@Override
	public IStringInputStream deriveStringInputStream()
	{
		return new BinaryToStringInputStream(createBinaryInputStream());
	}
}
