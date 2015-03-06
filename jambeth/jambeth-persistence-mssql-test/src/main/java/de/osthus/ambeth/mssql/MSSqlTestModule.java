package de.osthus.ambeth.mssql;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.jdbc.IConnectionTestDialect;

public class MSSqlTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(MSSqlModule.class);
		beanContextFactory.registerBean(MSSqlTestDialect.class).autowireable(IConnectionTestDialect.class);
	}
}
