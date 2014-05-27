package de.osthus.ambeth.cache.rootcachevalue;

import net.sf.cglib.reflect.FastConstructor;

public interface IRootCacheValueTypeProvider
{
	<V> FastConstructor getRootCacheValueType(Class<V> entityType);
}
