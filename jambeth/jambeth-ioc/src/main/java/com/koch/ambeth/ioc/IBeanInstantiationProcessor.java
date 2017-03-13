package com.koch.ambeth.ioc;

import java.util.List;

import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.BeanContextFactory;

public interface IBeanInstantiationProcessor
{
	Object instantiateBean(BeanContextFactory beanContextFactory, ServiceContext beanContext, IBeanConfiguration beanConfiguration, Class<?> beanType,
			List<IBeanConfiguration> beanConfHierarchy);
}
