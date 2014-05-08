package de.osthus.ambeth.stream.int32;

import de.osthus.ambeth.stream.IInputStream;

public interface IIntInputStream extends IInputStream
{
	boolean hasInt();

	int readInt();
}