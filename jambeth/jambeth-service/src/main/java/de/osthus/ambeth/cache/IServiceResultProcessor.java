package de.osthus.ambeth.cache;

import java.lang.annotation.Annotation;
import java.util.List;

import de.osthus.ambeth.merge.model.IObjRef;

public interface IServiceResultProcessor
{
	Object processServiceResult(Object result, List<IObjRef> objRefs, List<Object> entities, Class<?> expectedType, Object[] serviceRequestArgs,
			Annotation annotation);
}
