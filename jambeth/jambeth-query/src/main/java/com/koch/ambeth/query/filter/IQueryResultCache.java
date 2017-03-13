package com.koch.ambeth.query.filter;

import com.koch.ambeth.query.IQueryKey;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.IParamHolder;
import com.koch.ambeth.util.collections.IList;

public interface IQueryResultCache
{
	IQueryResultCacheItem getCacheItem(IQueryKey queryKey);

	IList<IObjRef> getQueryResult(IQueryKey queryKey, IQueryResultRetriever queryResultRetriever, byte idIndex, int offset, int length,
			IParamHolder<Integer> totalSize);
}