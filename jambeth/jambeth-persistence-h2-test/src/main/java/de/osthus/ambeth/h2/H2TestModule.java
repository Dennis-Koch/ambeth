package de.osthus.ambeth.h2;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.jdbc.IConnectionTestDialect;

public class H2TestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(H2Module.class);
		beanContextFactory.registerBean(H2TestDialect.class).autowireable(IConnectionTestDialect.class);
	}
}
