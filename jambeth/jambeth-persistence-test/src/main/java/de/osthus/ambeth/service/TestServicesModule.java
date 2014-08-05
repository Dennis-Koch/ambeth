package de.osthus.ambeth.service;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

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
