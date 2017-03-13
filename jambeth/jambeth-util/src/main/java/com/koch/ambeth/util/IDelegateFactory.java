package com.koch.ambeth.util;

public interface IDelegateFactory
{
	IDelegate createDelegate(Class<?> delegateType, Object target, String methodName);
}
