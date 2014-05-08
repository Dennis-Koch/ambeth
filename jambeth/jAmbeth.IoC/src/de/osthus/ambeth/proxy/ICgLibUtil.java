package de.osthus.ambeth.proxy;

public interface ICgLibUtil
{
	boolean isEnhanced(Class<?> enhancedClass);

	Class<?> getOriginalClass(Class<?> enhancedClass);

	Class<?>[] getAllInterfaces(Object obj, Class<?>... additional);
}