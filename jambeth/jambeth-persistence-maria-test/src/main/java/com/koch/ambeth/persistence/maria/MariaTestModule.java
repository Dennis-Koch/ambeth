package com.koch.ambeth.persistence.maria;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.persistence.jdbc.IConnectionTestDialect;
import com.koch.ambeth.persistence.maria.MariaModule;

public class MariaTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(MariaModule.class);
		beanContextFactory.registerBean(MariaTestDialect.class).autowireable(IConnectionTestDialect.class);
	}
}
