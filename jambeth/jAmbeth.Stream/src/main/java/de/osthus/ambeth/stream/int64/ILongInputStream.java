package de.osthus.ambeth.stream.int64;

import de.osthus.ambeth.stream.IInputStream;

public interface ILongInputStream extends IInputStream
{
	boolean hasLong();

	long readLong();
}