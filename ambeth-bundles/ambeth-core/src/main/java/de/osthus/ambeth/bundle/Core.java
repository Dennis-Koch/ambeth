package de.osthus.ambeth.bundle;

import de.osthus.ambeth.ioc.IocModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public class Core implements IBundleModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(IocModule.class);
	}
}
