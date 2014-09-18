package de.osthus.ambeth.ioc.postprocessor;

import java.util.Set;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IBeanPostProcessor;
import de.osthus.ambeth.ioc.IOrderedBeanPostProcessor;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.PostProcessorOrder;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public class OrderedPostProcessor implements IBeanPostProcessor, IOrderedBeanPostProcessor
{
	@Property
	protected PostProcessorOrder order;

	@Override
	public PostProcessorOrder getOrder()
	{
		return order;
	}

	@Override
	public Object postProcessBean(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Class<?> beanType,
			Object targetBean, Set<Class<?>> requestedTypes)
	{
		return targetBean;
	}
}
