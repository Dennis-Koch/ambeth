package com.koch.ambeth.ioc.factory;

import java.util.List;

import com.koch.ambeth.ioc.ServiceContext;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.util.collections.IList;

public interface IBeanContextInitializer
{
	void initializeBeanContext(ServiceContext beanContext, BeanContextFactory beanContextFactory);

	Object initializeBean(ServiceContext beanContext, BeanContextFactory beanContextFactory, IBeanConfiguration beanConfiguration, Object bean,
			List<IBeanConfiguration> beanConfHierarchy, boolean joinLifecycle);

	IList<IBeanConfiguration> fillParentHierarchyIfValid(ServiceContext beanContext, BeanContextFactory beanContextFactory, IBeanConfiguration beanConfiguration);

	Class<?> resolveTypeInHierarchy(List<IBeanConfiguration> beanConfigurations);

	Object instantiateBean(ServiceContext beanContext, BeanContextFactory beanContextFactory, IBeanConfiguration beanConfiguration, Class<?> beanType,
			List<IBeanConfiguration> beanConfHierarchy);
}
