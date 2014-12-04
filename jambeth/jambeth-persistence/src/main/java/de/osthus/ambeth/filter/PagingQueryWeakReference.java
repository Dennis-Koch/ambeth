package de.osthus.ambeth.filter;

import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.filter.model.IPagingRequest;
import de.osthus.ambeth.filter.model.IPagingResponse;
import de.osthus.ambeth.query.IQueryKey;

public class PagingQueryWeakReference<T> implements IPagingQuery<T>
{
	protected IPagingQuery<T> pagingQuery;

	public PagingQueryWeakReference(IPagingQuery<T> pagingQuery)
	{
		this.pagingQuery = pagingQuery;
	}

	@Override
	protected void finalize() throws Throwable
	{
		if (pagingQuery != null)
		{
			pagingQuery.dispose();
			pagingQuery = PagingQueryDisposed.getInstance();
		}
	}

	@Override
	public void dispose()
	{
		if (pagingQuery != null)
		{
			pagingQuery.dispose();
			pagingQuery = PagingQueryDisposed.getInstance();
		}
	}

	@Override
	public IPagingRequest createRequest(int pageNumber, int sizePerPage)
	{
		return pagingQuery.createRequest(pageNumber, sizePerPage);
	}

	@Override
	public IQueryKey getQueryKey(IMap<Object, Object> nameToValueMap)
	{
		return pagingQuery.getQueryKey(nameToValueMap);
	}

	@Override
	public IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest, String alternateIdPropertyName)
	{
		return pagingQuery.retrieveRefs(pagingRequest, alternateIdPropertyName);
	}

	@Override
	public IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest)
	{
		return pagingQuery.retrieveRefs(pagingRequest);
	}

	@Override
	public IPagingResponse<T> retrieve(IPagingRequest pagingRequest)
	{
		return pagingQuery.retrieve(pagingRequest);
	}

	@Override
	public IPagingQuery<T> param(Object paramKey, Object param)
	{
		IPagingQuery<T> resultQuery = pagingQuery.param(paramKey, param);
		if (resultQuery == pagingQuery)
		{
			// Query instance is the same, so our weakreference may remain the same, too
			return this;
		}
		return new PagingQueryWeakReference<T>(resultQuery);
	}
}
