package de.osthus.ambeth.stream.int64;

import de.osthus.ambeth.stream.IInputSource;

public interface ILongInputSource extends IInputSource
{
	ILongInputStream deriveLongInputStream();
}
