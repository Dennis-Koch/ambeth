package de.osthus.ambeth.filter;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.query.IQueryKey;
import de.osthus.ambeth.util.IParamHolder;

public interface IQueryResultCache
{
	IQueryResultCacheItem getCacheItem(IQueryKey queryKey);

	IList<IObjRef> getQueryResult(IQueryKey queryKey, IQueryResultRetriever queryResultRetriever, byte idIndex, int offset, int length,
			IParamHolder<Integer> totalSize);
}