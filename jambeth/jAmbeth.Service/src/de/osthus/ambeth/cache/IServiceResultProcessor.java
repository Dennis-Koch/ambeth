package de.osthus.ambeth.cache;

import java.util.List;

public interface IServiceResultProcessor
{
	Object processServiceResult(Object result, List<Object> entities, Class<?> expectedType, Object[] serviceRequestArgs);
}
