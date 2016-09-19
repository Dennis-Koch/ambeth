package de.osthus.ambeth.sqlite;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.jdbc.IConnectionTestDialect;
import de.osthus.ambeth.sqlite.SQLiteModule;

public class SQLiteTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(SQLiteModule.class);
		beanContextFactory.registerBean(SQLiteTestDialect.class).autowireable(IConnectionTestDialect.class);
	}
}
