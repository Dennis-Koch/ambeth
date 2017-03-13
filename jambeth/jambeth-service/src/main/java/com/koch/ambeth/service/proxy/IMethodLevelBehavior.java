package com.koch.ambeth.service.proxy;

import java.lang.reflect.Method;

public interface IMethodLevelBehavior<T>
{
	T getDefaultBehaviour();

	T getBehaviourOfMethod(Method method);
}