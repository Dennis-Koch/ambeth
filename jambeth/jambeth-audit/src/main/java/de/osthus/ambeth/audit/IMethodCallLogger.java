package de.osthus.ambeth.audit;

import java.lang.reflect.Method;

public interface IMethodCallLogger
{
	IMethodCallHandle logMethodCallStart(Method method);

	void logMethodCallFinish(IMethodCallHandle methodCallHandle);
}