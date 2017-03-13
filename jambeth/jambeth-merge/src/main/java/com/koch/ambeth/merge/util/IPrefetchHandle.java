package com.koch.ambeth.merge.util;

public interface IPrefetchHandle
{
	IPrefetchState prefetch(Object objects);
	
	IPrefetchState prefetch(Object... objects);

	IPrefetchHandle union(IPrefetchHandle otherPrefetchHandle);
}