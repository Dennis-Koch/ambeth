package de.osthus.ambeth.filter;

import java.util.Map;

import de.osthus.ambeth.filter.model.IPagingRequest;
import de.osthus.ambeth.filter.model.IPagingResponse;
import de.osthus.ambeth.query.IQueryKey;

public final class PagingQueryDisposed<T> implements IPagingQuery<T>
{
	@SuppressWarnings("rawtypes")
	private static final PagingQueryDisposed instance = new PagingQueryDisposed();

	@SuppressWarnings("unchecked")
	public static final <T> IPagingQuery<T> getInstance()
	{
		return instance;
	}

	private PagingQueryDisposed()
	{
		// intended blank
	}

	@Override
	public void dispose()
	{
		// intended blank
	}

	protected RuntimeException createException()
	{
		return new UnsupportedOperationException(
				"This query has already been disposed. This seems like a memory leak in your application if you refer to illegal of query-handles");
	}

	@Override
	public IPagingRequest createRequest(int pageNumber, int sizePerPage)
	{
		throw createException();
	}

	@Override
	public IQueryKey getQueryKey(Map<Object, Object> nameToValueMap)
	{
		throw createException();
	}

	@Override
	public IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest, String alternateIdPropertyName)
	{
		throw createException();
	}

	@Override
	public IPagingResponse<T> retrieveRefs(IPagingRequest pagingRequest)
	{
		throw createException();
	}

	@Override
	public IPagingResponse<T> retrieve(IPagingRequest pagingRequest)
	{
		throw createException();
	}

	@Override
	public IPagingQuery<T> param(Object paramKey, Object param)
	{
		throw createException();
	}
}
