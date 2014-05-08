package de.osthus.ambeth.stream.float32;

import de.osthus.ambeth.stream.IInputSource;

public interface IFloatInputSource extends IInputSource
{
	IFloatInputStream deriveFloatInputStream();
}
