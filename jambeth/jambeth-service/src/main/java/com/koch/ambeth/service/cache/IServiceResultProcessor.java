package com.koch.ambeth.service.cache;

import java.lang.annotation.Annotation;
import java.util.List;

import com.koch.ambeth.service.merge.model.IObjRef;

public interface IServiceResultProcessor
{
	Object processServiceResult(Object result, List<IObjRef> objRefs, List<Object> entities, Class<?> expectedType, Object[] serviceRequestArgs,
			Annotation annotation);
}
