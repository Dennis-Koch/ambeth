package com.koch.ambeth.cache.stream.bytebuffer;

import java.io.RandomAccessFile;

public interface IFileReadDelegate<T>
{
	T read(RandomAccessFile raFile) throws Throwable;
}
