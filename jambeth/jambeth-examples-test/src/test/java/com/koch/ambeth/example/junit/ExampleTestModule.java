package com.koch.ambeth.example.junit;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

public class ExampleTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("myBean1", MyBean1.class).autowireable(MyBean1.class);

		beanContextFactory.registerBean("myBean2", MyBean2.class);
	}
}
