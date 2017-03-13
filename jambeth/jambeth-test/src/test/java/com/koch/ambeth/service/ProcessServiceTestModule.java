package com.koch.ambeth.service;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.transfer.ITestService;

public class ProcessServiceTestModule implements IInitializingModule
{
	public static final String TEST_SERVICE_NAME = "testService";

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(TEST_SERVICE_NAME, TestService.class).autowireable(ITestService.class);
	}
}
