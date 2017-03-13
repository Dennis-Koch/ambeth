package com.koch.ambeth.query.filter;

import com.koch.ambeth.filter.IPagingRequest;
import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.query.IQueryKey;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.collections.IMap;

public interface IPagingQuery<T> extends IDisposable
{
	IPagingRequest createRequest(int pageNumber, int sizePerPage);

	IQueryKey getQueryKey(IMap<Object, Object> nameToValueMap);

	IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest);

	IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest, String alternateIdPropertyName);

	IPagingResponse<T> retrieve(IPagingRequest pagingRequest);

	IPagingQuery<T> param(Object paramKey, Object param);
}
