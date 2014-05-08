package de.osthus.ambeth.ioc.performance;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

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
