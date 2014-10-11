package de.osthus.ambeth.testutil;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.config.PrecedenceType;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public class DataSetupExecutorModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAnonymousBean(DataSetupExecutor.class).autowireable(DataSetupExecutor.class).precedence(PrecedenceType.LOWEST);
	}
}
