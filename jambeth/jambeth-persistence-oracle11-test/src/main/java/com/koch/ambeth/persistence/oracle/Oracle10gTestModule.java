package com.koch.ambeth.persistence.oracle;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.persistence.jdbc.IConnectionTestDialect;
import com.koch.ambeth.persistence.oracle.Oracle10gModule;

public class Oracle10gTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(Oracle10gModule.class);
		beanContextFactory.registerBean(Oracle10gTestDialect.class).autowireable(IConnectionTestDialect.class);
	}
}
