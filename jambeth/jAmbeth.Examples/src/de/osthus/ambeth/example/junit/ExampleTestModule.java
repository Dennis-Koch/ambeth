package de.osthus.ambeth.example.junit;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public class ExampleTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("myBean1", MyBean1.class).autowireable(MyBean1.class);

		beanContextFactory.registerBean("myBean2", MyBean2.class);
	}
}
