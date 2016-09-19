package de.osthus.ambeth.log;

import java.util.Set;

import net.sf.cglib.proxy.Callback;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.IOrderedBeanProcessor;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.ProcessorOrder;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.interceptor.LogInterceptor;
import de.osthus.ambeth.proxy.AbstractCascadePostProcessor;
import de.osthus.ambeth.proxy.ICascadedInterceptor;

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
