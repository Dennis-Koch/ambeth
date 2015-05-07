package de.osthus.ambeth.pg;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.jdbc.IConnectionTestDialect;
import de.osthus.ambeth.pg.PostgresModule;

public class PostgresTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(PostgresModule.class);
		beanContextFactory.registerBean(PostgresTestDialect.class).autowireable(IConnectionTestDialect.class);
	}
}
