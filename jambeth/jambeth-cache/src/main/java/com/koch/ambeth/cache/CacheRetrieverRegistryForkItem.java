package com.koch.ambeth.cache;

import com.koch.ambeth.cache.service.ICacheRetriever;
import com.koch.ambeth.util.collections.IList;

public class CacheRetrieverRegistryForkItem<V>
{
	public final ICacheRetriever cacheRetriever;
	public final IList<V> paramList;

	public CacheRetrieverRegistryForkItem(ICacheRetriever cacheRetriever, IList<V> paramList)
	{
		this.cacheRetriever = cacheRetriever;
		this.paramList = paramList;
	}
}
