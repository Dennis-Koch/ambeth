package de.osthus.ambeth.audit;

import java.lang.reflect.Method;

public interface IMethodCallLogger
{
	void logMethodCall(Method method);
}