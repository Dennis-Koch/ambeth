package de.osthus.ambeth.log;

import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.ioc.IServiceContext;

public interface ILoggerCache
{
	ILogger getCachedLogger(IServiceContext serviceContext, Class<?> loggerBeanType);

	ILogger getCachedLogger(IProperties properties, Class<?> loggerBeanType);
}