package de.osthus.ambeth.cache;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.service.ICacheRetriever;

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
