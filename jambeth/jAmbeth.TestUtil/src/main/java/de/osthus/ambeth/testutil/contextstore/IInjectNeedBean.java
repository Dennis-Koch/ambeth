package de.osthus.ambeth.testutil.contextstore;

import de.osthus.ambeth.ioc.config.IBeanConfiguration;

public interface IInjectNeedBean
{
	IInjectNeedTargetContext bean(String beanName);

	IInjectNeedTargetContext bean(Class<?> beanInterface);

	IInjectNeedTargetContext bean(IBeanConfiguration beanConfig);
}
