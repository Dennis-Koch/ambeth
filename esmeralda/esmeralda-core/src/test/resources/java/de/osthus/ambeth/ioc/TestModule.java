package de.osthus.ambeth.ioc;

import de.osthus.ambeth.demo.ITestInterface;
import de.osthus.ambeth.demo.TestService1;
import de.osthus.ambeth.demo.TestService2;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public class TestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAnonymousBean(TestService1.class).autowireable(ITestInterface.class);
		beanContextFactory.registerBean("testService2", TestService2.class);
	}
}
