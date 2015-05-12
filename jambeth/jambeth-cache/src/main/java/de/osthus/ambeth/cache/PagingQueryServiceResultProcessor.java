package de.osthus.ambeth.cache;

import java.lang.annotation.Annotation;
import java.util.List;

import de.osthus.ambeth.annotation.Find;
import de.osthus.ambeth.annotation.QueryResultType;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.filter.model.IPagingResponse;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;

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
