package com.koch.ambeth.training.travelguides.proxy;

import java.util.Set;

import com.koch.ambeth.annotation.AnnotationCache;
import com.koch.ambeth.ioc.IOrderedBeanPostProcessor;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.PostProcessorOrder;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.proxy.AbstractCascadePostProcessor;
import com.koch.ambeth.proxy.ICascadedInterceptor;
import com.koch.ambeth.training.travelguides.annotation.LogCalls;
import com.koch.ambeth.training.travelguides.interceptor.LogCallsInterceptor;

public class LogCallsPostProcessor extends AbstractCascadePostProcessor implements IOrderedBeanPostProcessor
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final AnnotationCache<LogCalls> logCallsContextCache = new AnnotationCache<LogCalls>(LogCalls.class)
	{
		@Override
		protected boolean annotationEquals(LogCalls left, LogCalls right)
		{
			return true;
		}
	};

	@Override
	public PostProcessorOrder getOrder()
	{
		return PostProcessorOrder.LOWER;
	}

	@Override
	protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext,
			IBeanConfiguration beanConfiguration, Class<?> type, Set<Class<?>> requestedTypes)
	{
		// log.info(type.getName());
		LogCalls logCalls = logCallsContextCache.getAnnotation(type);

		if (logCalls == null)
		{
			return null;
		}

		LogCallsInterceptor logCallsInterceptor = new LogCallsInterceptor();
		if (beanContext.isRunning())
		{
			return beanContext.registerWithLifecycle(logCallsInterceptor)//
					.finish();
		}
		beanContextFactory.registerWithLifecycle(logCallsInterceptor);//
		return logCallsInterceptor;
	}
}
