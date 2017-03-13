package com.koch.ambeth.cache.stream.strings;

import com.koch.ambeth.cache.stream.AbstractInputSourceValueHolder;
import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.stream.strings.BinaryToStringInputStream;
import com.koch.ambeth.stream.strings.IStringInputSource;
import com.koch.ambeth.stream.strings.IStringInputStream;

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
