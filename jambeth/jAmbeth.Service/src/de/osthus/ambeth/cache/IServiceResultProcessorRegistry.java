package de.osthus.ambeth.cache;

public interface IServiceResultProcessorRegistry
{
	IServiceResultProcessor getServiceResultProcessor(Class<?> returnType);
}