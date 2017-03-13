package com.koch.ambeth.informationbus.testutil.contextstore;

import com.koch.ambeth.ioc.config.IBeanConfiguration;

public interface IInjectNeedTargetBean
{
	IInjectNeedTargetProperty intoBean(String beanName);

	IInjectNeedTargetProperty intoBean(Class<?> beanInterface);

	IInjectNeedTargetProperty intoBean(IBeanConfiguration beanConfig);
}
