package com.koch.ambeth.service.log;

import java.util.Set;

import com.koch.ambeth.ioc.IOrderedBeanProcessor;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.ProcessorOrder;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.log.interceptor.LogInterceptor;
import com.koch.ambeth.service.proxy.AbstractCascadePostProcessor;
import com.koch.ambeth.util.proxy.ICascadedInterceptor;

import net.sf.cglib.proxy.Callback;

public class LoggingPostProcessor extends AbstractCascadePostProcessor implements IOrderedBeanProcessor
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = ServiceConfigurationConstants.WrapAllInteractions, defaultValue = "false")
	protected boolean wrapAllInteractions;

	@Property(name = ServiceConfigurationConstants.LogShortNames, defaultValue = "false")
	protected boolean printShortStringNames;

	@Override
	protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext,
			IBeanConfiguration beanConfiguration, Class<?> type, Set<Class<?>> requestedTypes)
	{
		if (!type.isAnnotationPresent(LogException.class) && (!wrapAllInteractions || Callback.class.isAssignableFrom(type)))
		{
			return null;
		}
		LogInterceptor logInterceptor = new LogInterceptor();
		if (beanContext.isRunning())
		{
			return beanContext.registerWithLifecycle(logInterceptor).finish();
		}
		beanContextFactory.registerWithLifecycle(logInterceptor);
		return logInterceptor;
	}

	@Override
	public ProcessorOrder getOrder()
	{
		return ProcessorOrder.HIGHEST;
	}
}
