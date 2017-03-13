package com.koch.ambeth.persistence.sqlite;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.persistence.jdbc.IConnectionTestDialect;
import com.koch.ambeth.persistence.sqlite.SQLiteModule;

public class SQLiteTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(SQLiteModule.class);
		beanContextFactory.registerBean(SQLiteTestDialect.class).autowireable(IConnectionTestDialect.class);
	}
}
