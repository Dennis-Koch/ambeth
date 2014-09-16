package de.osthus.ambeth.h2;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.jdbc.IConnectionTestDialect;

public class H2TestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAnonymousBean(H2Module.class);
		beanContextFactory.registerAnonymousBean(H2TestDialect.class).autowireable(IConnectionTestDialect.class);
	}
}
