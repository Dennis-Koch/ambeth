package de.osthus.ambeth.oracle;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.jdbc.IConnectionTestDialect;

public class Oracle10gTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAnonymousBean(Oracle10gModule.class);
		beanContextFactory.registerAnonymousBean(Oracle10gTestDialect.class).autowireable(IConnectionTestDialect.class);
	}
}
