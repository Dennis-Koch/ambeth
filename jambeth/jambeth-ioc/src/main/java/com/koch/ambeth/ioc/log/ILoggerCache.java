package com.koch.ambeth.ioc.log;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.util.config.IProperties;

public interface ILoggerCache
{
	ILogger getCachedLogger(IServiceContext serviceContext, Class<?> loggerBeanType);

	ILogger getCachedLogger(IProperties properties, Class<?> loggerBeanType);
}