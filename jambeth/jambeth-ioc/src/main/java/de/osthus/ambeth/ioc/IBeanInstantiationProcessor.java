package de.osthus.ambeth.ioc;

import java.util.List;

import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;

public interface IBeanInstantiationProcessor
{
	Object instantiateBean(BeanContextFactory beanContextFactory, ServiceContext beanContext, IBeanConfiguration beanConfiguration, Class<?> beanType,
			List<IBeanConfiguration> beanConfHierarchy);
}
