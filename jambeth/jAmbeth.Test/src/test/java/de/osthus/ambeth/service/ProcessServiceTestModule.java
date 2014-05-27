package de.osthus.ambeth.service;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.transfer.ITestService;

public class ProcessServiceTestModule implements IInitializingModule
{
	public static final String TEST_SERVICE_NAME = "testService";

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(TEST_SERVICE_NAME, TestService.class).autowireable(ITestService.class);
	}
}
