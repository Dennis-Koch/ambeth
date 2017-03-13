package com.koch.ambeth.persistence.filter;

import java.util.Map;

import com.koch.ambeth.filter.IPagingRequest;
import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.query.filter.IPagingQuery;

public interface IPagingQueryIntern<T> extends IPagingQuery<T>
{
	IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest, Map<Object, Object> paramMap);

	IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest, String alternateIdPropertyName, Map<Object, Object> paramMap);

	IPagingResponse<T> retrieve(IPagingRequest pagingRequest, Map<Object, Object> paramMap);
}
