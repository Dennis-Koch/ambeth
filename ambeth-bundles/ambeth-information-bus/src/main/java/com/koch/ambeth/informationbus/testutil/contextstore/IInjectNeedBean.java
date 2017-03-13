package com.koch.ambeth.informationbus.testutil.contextstore;

import com.koch.ambeth.ioc.config.IBeanConfiguration;

public interface IInjectNeedBean
{
	IInjectNeedTargetContext bean(String beanName);

	IInjectNeedTargetContext bean(Class<?> beanInterface);

	IInjectNeedTargetContext bean(IBeanConfiguration beanConfig);
}
