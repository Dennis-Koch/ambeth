package de.osthus.ambeth.proxy;

import java.lang.reflect.Method;

public interface IMethodLevelBehavior<T>
{
	T getDefaultBehaviour();

	T getBehaviourOfMethod(Method method);
}