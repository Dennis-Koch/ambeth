package com.koch.ambeth.persistence.h2;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.persistence.h2.H2Module;
import com.koch.ambeth.persistence.jdbc.IConnectionTestDialect;

public class H2TestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(H2Module.class);
		beanContextFactory.registerBean(H2TestDialect.class).autowireable(IConnectionTestDialect.class);
	}
}
