package com.koch.ambeth.service;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

public class TestServicesModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance(TestServicesModule.class)
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("materialService", MaterialService.class).autowireable(IMaterialService.class);
	}
}
