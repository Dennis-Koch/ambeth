package de.osthus.ambeth.h2;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public class H2ConnectionModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		// Intended blank
	}
}
