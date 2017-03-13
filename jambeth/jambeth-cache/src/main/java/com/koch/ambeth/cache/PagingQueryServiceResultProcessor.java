package com.koch.ambeth.cache;

import java.lang.annotation.Annotation;
import java.util.List;

import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.cache.IServiceResultProcessor;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.annotation.Find;
import com.koch.ambeth.util.annotation.QueryResultType;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class PagingQueryServiceResultProcessor implements IServiceResultProcessor
{

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public Object processServiceResult(Object result, List<IObjRef> objRefs, List<Object> entities, Class<?> expectedType, Object[] serviceRequestArgs,
			Annotation annotation)
	{
		@SuppressWarnings("unchecked")
		IPagingResponse<Object> pagingResponse = (IPagingResponse<Object>) result;

		QueryResultType queryResultType = QueryResultType.REFERENCES;
		if (annotation instanceof Find)
		{
			queryResultType = ((Find) annotation).resultType();
		}
		switch (queryResultType)
		{
			case BOTH:
				pagingResponse.setRefResult(objRefs);
				pagingResponse.setResult(entities);
				break;
			case ENTITIES:
				pagingResponse.setRefResult(null);
				pagingResponse.setResult(entities);
				break;
			case REFERENCES:
				pagingResponse.setRefResult(objRefs);
				pagingResponse.setResult(null);
				break;
			default:
				throw RuntimeExceptionUtil.createEnumNotSupportedException(queryResultType);
		}
		return pagingResponse;
	}
}
