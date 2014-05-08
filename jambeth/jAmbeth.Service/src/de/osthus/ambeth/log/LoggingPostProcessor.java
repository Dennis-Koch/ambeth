package de.osthus.ambeth.log;

import java.util.Set;

import net.sf.cglib.proxy.Callback;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IOrderedBeanPostProcessor;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.PostProcessorOrder;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.interceptor.LogInterceptor;
import de.osthus.ambeth.proxy.AbstractCascadePostProcessor;
import de.osthus.ambeth.proxy.ICascadedInterceptor;
import de.osthus.ambeth.service.config.ConfigurationConstants;

public class LoggingPostProcessor extends AbstractCascadePostProcessor implements IOrderedBeanPostProcessor
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected boolean wrapAllInteractions;

	protected boolean printShortStringNames;

	@Property(name = ConfigurationConstants.WrapAllInteractions, defaultValue = "false")
	public void setWrapAllInteractions(boolean wrapAllInteractions)
	{
		this.wrapAllInteractions = wrapAllInteractions;
	}

	@Property(name = ConfigurationConstants.LogShortNames, defaultValue = "false")
	public void setPrintShortStringNames(boolean printShortStringNames)
	{
		this.printShortStringNames = printShortStringNames;
	}

	@Override
	protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext,
			IBeanConfiguration beanConfiguration, Class<?> type, Set<Class<?>> requestedTypes)
	{
		if (!wrapAllInteractions || Callback.class.isAssignableFrom(type))
		{
			return null;
		}
		if (requestedTypes.size() > 2)
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
	public PostProcessorOrder getOrder()
	{
		// A logger interceptor has to be OUTERMOST of all potential postprocessor-created proxies, to get each call correctly despite any custom behavior of
		// other proxies
		return PostProcessorOrder.LOWEST;
	}
}
