package de.osthus.ambeth.proxy;

import java.lang.reflect.Method;

public interface IMethodLevelBehaviour<T>
{
	T getDefaultBehaviour();

	T getBehaviourOfMethod(Method method);
}