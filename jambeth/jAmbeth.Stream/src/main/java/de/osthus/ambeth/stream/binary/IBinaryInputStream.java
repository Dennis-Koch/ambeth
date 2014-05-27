package de.osthus.ambeth.stream.binary;

import java.io.Closeable;

public interface IBinaryInputStream extends Closeable
{
	int readByte();
}