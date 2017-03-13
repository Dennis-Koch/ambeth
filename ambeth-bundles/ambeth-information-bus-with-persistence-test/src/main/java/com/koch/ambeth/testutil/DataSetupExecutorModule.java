package com.koch.ambeth.testutil;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.config.PrecedenceType;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

public class DataSetupExecutorModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(DataSetupExecutor.class).autowireable(DataSetupExecutor.class).precedence(PrecedenceType.LOWEST);
	}
}
