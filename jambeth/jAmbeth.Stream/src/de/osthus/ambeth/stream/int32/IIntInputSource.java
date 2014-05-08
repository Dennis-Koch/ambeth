package de.osthus.ambeth.stream.int32;

import de.osthus.ambeth.stream.IInputSource;

public interface IIntInputSource extends IInputSource
{
	IIntInputStream deriveIntInputStream();
}
