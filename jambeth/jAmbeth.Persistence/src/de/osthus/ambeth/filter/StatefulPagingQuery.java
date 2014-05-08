package de.osthus.ambeth.filter;

import java.util.Map;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.filter.model.IPagingRequest;
import de.osthus.ambeth.filter.model.IPagingResponse;
import de.osthus.ambeth.query.IQueryKey;

public class StatefulPagingQuery<T> implements IPagingQuery<T>
{
	protected IPagingQueryIntern<T> pagingQuery;

	protected final HashMap<Object, Object> paramMap = new HashMap<Object, Object>();

	public StatefulPagingQuery(IPagingQueryIntern<T> pagingQuery)
	{
		this.pagingQuery = pagingQuery;
	}

	@Override
	public void dispose()
	{
		pagingQuery = null;
		paramMap.clear();
	}

	@Override
	public IPagingRequest createRequest(int pageNumber, int sizePerPage)
	{
		return pagingQuery.createRequest(pageNumber, sizePerPage);
	}

	@Override
	public IQueryKey getQueryKey(Map<Object, Object> nameToValueMap)
	{
		return pagingQuery.getQueryKey(nameToValueMap);
	}

	@Override
	public IPagingQuery<T> param(Object paramKey, Object param)
	{
		if (!paramMap.putIfNotExists(paramKey, param))
		{
			throw new IllegalArgumentException("Parameter '" + paramKey + "' already added with value '" + paramMap.get(paramKey) + "'");
		}
		return this;
	}

	@Override
	public IPagingResponse<T> retrieve(IPagingRequest pagingRequest)
	{
		return pagingQuery.retrieve(pagingRequest, paramMap);
	}

	@Override
	public IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest)
	{
		return pagingQuery.retrieveRefs(pagingRequest, paramMap);
	}

	@Override
	public IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest, String alternateIdPropertyName)
	{
		return pagingQuery.retrieveRefs(pagingRequest, alternateIdPropertyName, paramMap);
	}
}
