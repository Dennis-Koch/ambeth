package com.koch.ambeth.service.cache;

public interface IServiceResultProcessorRegistry
{
	IServiceResultProcessor getServiceResultProcessor(Class<?> returnType);
}