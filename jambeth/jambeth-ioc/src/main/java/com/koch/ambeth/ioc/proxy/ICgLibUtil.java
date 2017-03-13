package com.koch.ambeth.ioc.proxy;

public interface ICgLibUtil
{
	boolean isEnhanced(Class<?> enhancedClass);

	Class<?> getOriginalClass(Class<?> enhancedClass);

	Class<?>[] getAllInterfaces(Object obj, Class<?>... additional);
}