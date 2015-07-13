package de.osthus.ambeth.util;

import de.osthus.ambeth.collections.Tuple2KeyEntry;
import de.osthus.ambeth.collections.Tuple2KeyHashMap;

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