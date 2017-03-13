package com.koch.ambeth.service.name;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.service.TestService;

public class ProcessServiceNamedTestModule implements IInitializingModule
{
	public static final String TEST_SERVICE_NAME = "testService";

	public static final String TEST_SERVICE_2_NAME = "testService2";

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(TEST_SERVICE_NAME, TestService.class).autowireable(com.koch.ambeth.transfer.ITestService.class);

		beanContextFactory.registerBean(TEST_SERVICE_2_NAME, TestService2.class).autowireable(ITestService.class);
	}
}
