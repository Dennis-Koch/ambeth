package com.koch.ambeth.ioc;

import java.util.Set;

import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

public interface IBeanPostProcessor
{
	Object postProcessBean(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Class<?> beanType,
			Object targetBean, Set<Class<?>> requestedTypes);
}
