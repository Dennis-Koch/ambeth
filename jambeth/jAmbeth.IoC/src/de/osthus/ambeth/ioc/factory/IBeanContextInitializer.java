package de.osthus.ambeth.ioc.factory;

import java.util.List;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.ServiceContext;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;

public interface IBeanContextInitializer
{
	void initializeBeanContext(ServiceContext beanContext, BeanContextFactory beanContextFactory);

	Object initializeBean(ServiceContext beanContext, BeanContextFactory beanContextFactory, IBeanConfiguration beanConfiguration, Object bean,
			List<IBeanConfiguration> beanConfHierarchy, boolean joinLifecycle);

	IList<IBeanConfiguration> fillParentHierarchyIfValid(ServiceContext beanContext, BeanContextFactory beanContextFactory, IBeanConfiguration beanConfiguration);

	Class<?> resolveTypeInHierarchy(List<IBeanConfiguration> beanConfigurations);
}
