package com.koch.ambeth.ioc.performance;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

public class IocPerformanceTestModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = IocPerformanceTest.count_prop)
	protected int count;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		for (int a = count; a-- > 0;)
		{
			beanContextFactory.registerBean("name" + a, TestBean.class).propertyValue("Value", "value");
		}
	}
}
