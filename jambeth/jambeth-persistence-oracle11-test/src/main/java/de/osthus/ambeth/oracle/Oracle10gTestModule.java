package de.osthus.ambeth.oracle;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.jdbc.IConnectionTestDialect;

public class Oracle10gTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(Oracle10gModule.class);
		beanContextFactory.registerBean(Oracle10gTestDialect.class).autowireable(IConnectionTestDialect.class);
	}
}
