package de.osthus.ambeth.stream.float64;

import de.osthus.ambeth.stream.IInputSource;

public interface IDoubleInputSource extends IInputSource
{
	IDoubleInputStream deriveDoubleInputStream();
}
