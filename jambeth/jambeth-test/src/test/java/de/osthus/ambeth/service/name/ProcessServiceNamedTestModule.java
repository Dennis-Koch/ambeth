package de.osthus.ambeth.service.name;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.service.TestService;

public class ProcessServiceNamedTestModule implements IInitializingModule
{
	public static final String TEST_SERVICE_NAME = "testService";

	public static final String TEST_SERVICE_2_NAME = "testService2";

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(TEST_SERVICE_NAME, TestService.class).autowireable(de.osthus.ambeth.transfer.ITestService.class);

		beanContextFactory.registerBean(TEST_SERVICE_2_NAME, TestService2.class).autowireable(ITestService.class);
	}
}
