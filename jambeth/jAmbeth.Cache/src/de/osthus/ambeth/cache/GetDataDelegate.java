package de.osthus.ambeth.cache;

import java.util.List;

import de.osthus.ambeth.service.ICacheRetriever;

public interface GetDataDelegate<V, R>
{
	List<R> invoke(ICacheRetriever cacheRetriever, List<V> arguments);
}