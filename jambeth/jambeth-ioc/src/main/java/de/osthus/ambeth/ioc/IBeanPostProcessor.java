package de.osthus.ambeth.ioc;

import java.util.Set;

import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public interface IBeanPostProcessor
{
	Object postProcessBean(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Class<?> beanType,
			Object targetBean, Set<Class<?>> requestedTypes);
}
