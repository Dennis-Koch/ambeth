package de.osthus.ambeth.filter;

import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.filter.model.IPagingRequest;
import de.osthus.ambeth.filter.model.IPagingResponse;
import de.osthus.ambeth.query.IQueryKey;
import de.osthus.ambeth.util.IDisposable;

public interface IPagingQuery<T> extends IDisposable
{
	IPagingRequest createRequest(int pageNumber, int sizePerPage);

	IQueryKey getQueryKey(IMap<Object, Object> nameToValueMap);

	IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest);

	IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest, String alternateIdPropertyName);

	IPagingResponse<T> retrieve(IPagingRequest pagingRequest);

	IPagingQuery<T> param(Object paramKey, Object param);
}
