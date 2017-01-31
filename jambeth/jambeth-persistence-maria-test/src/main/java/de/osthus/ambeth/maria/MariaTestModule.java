package de.osthus.ambeth.maria;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.jdbc.IConnectionTestDialect;

public class MariaTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(MariaModule.class);
		beanContextFactory.registerBean(MariaTestDialect.class).autowireable(IConnectionTestDialect.class);
	}
}
