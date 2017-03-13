package com.koch.ambeth.cache;

import java.util.List;

import com.koch.ambeth.cache.service.ICacheRetriever;

public interface GetDataDelegate<V, R>
{
	List<R> invoke(ICacheRetriever cacheRetriever, List<V> arguments);
}