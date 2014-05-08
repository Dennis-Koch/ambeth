package de.osthus.ambeth.testutil.contextstore;

import de.osthus.ambeth.ioc.config.IBeanConfiguration;

public interface IInjectNeedTargetBean
{
	IInjectNeedTargetProperty intoBean(String beanName);

	IInjectNeedTargetProperty intoBean(Class<?> beanInterface);

	IInjectNeedTargetProperty intoBean(IBeanConfiguration beanConfig);
}
