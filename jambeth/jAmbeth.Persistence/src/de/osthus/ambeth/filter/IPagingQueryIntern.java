package de.osthus.ambeth.filter;

import java.util.Map;

import de.osthus.ambeth.filter.model.IPagingRequest;
import de.osthus.ambeth.filter.model.IPagingResponse;

public interface IPagingQueryIntern<T> extends IPagingQuery<T>
{
	IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest, Map<Object, Object> paramMap);

	IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest, String alternateIdPropertyName, Map<Object, Object> paramMap);

	IPagingResponse<T> retrieve(IPagingRequest pagingRequest, Map<Object, Object> paramMap);
}
