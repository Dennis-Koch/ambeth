package de.osthus.ambeth.training.travelguides.proxy;

import java.util.Set;

import de.osthus.ambeth.annotation.AnnotationCache;
import de.osthus.ambeth.ioc.IOrderedBeanPostProcessor;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.PostProcessorOrder;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.AbstractCascadePostProcessor;
import de.osthus.ambeth.proxy.ICascadedInterceptor;
import de.osthus.ambeth.training.travelguides.annotation.LogCalls;
import de.osthus.ambeth.training.travelguides.interceptor.LogCallsInterceptor;

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
