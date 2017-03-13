package com.koch.ambeth.audit.server;

import java.lang.reflect.Method;

public interface IMethodCallLogger
{
	IMethodCallHandle logMethodCallStart(Method method, Object[] args);

	void logMethodCallFinish(IMethodCallHandle methodCallHandle);
}