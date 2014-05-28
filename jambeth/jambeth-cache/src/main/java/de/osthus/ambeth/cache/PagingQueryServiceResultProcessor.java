package de.osthus.ambeth.cache;

import java.util.List;

import de.osthus.ambeth.filter.model.IPagingResponse;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class PagingQueryServiceResultProcessor implements IServiceResultProcessor
{

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public Object processServiceResult(Object result, List<Object> entities, Class<?> expectedType, Object[] serviceRequestArgs)
	{
		@SuppressWarnings("unchecked")
		IPagingResponse<Object> pagingResponse = (IPagingResponse<Object>) result;

		pagingResponse.setRefResult(null);
		pagingResponse.setResult(entities);
		return pagingResponse;
	}
}
