package com.koch.ambeth.persistence.pg;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.persistence.jdbc.IConnectionTestDialect;
import com.koch.ambeth.persistence.pg.PostgresModule;

public class PostgresTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(PostgresModule.class);
		beanContextFactory.registerBean(PostgresTestDialect.class).autowireable(IConnectionTestDialect.class);
	}
}
