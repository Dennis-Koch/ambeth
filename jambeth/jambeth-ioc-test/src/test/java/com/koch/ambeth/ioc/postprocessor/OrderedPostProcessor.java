package com.koch.ambeth.ioc.postprocessor;

import java.util.Set;

import com.koch.ambeth.ioc.IBeanPostProcessor;
import com.koch.ambeth.ioc.IOrderedBeanProcessor;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.ProcessorOrder;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

public class OrderedPostProcessor implements IBeanPostProcessor, IOrderedBeanProcessor
{
	@Property
	protected ProcessorOrder order;

	@Override
	public ProcessorOrder getOrder()
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
