package com.koch.ambeth.cache.util;

import com.koch.ambeth.util.collections.Tuple2KeyEntry;
import com.koch.ambeth.util.collections.Tuple2KeyHashMap;

public class AlreadyHandledSet extends Tuple2KeyHashMap<Object, PrefetchPath[], Boolean>
{
	@Override
	protected boolean equalKeys(Object obj, PrefetchPath[] prefetchPaths, Tuple2KeyEntry<Object, PrefetchPath[], Boolean> entry)
	{
		return obj == entry.getKey1() && prefetchPaths == entry.getKey2();
	}

	@Override
	protected int extractHash(Object obj, PrefetchPath[] prefetchPaths)
	{
		if (prefetchPaths == null)
		{
			return System.identityHashCode(obj);
		}
		return System.identityHashCode(obj) ^ prefetchPaths.hashCode();
	}
}