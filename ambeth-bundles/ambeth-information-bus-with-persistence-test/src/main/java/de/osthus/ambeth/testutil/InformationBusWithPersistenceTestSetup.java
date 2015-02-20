package de.osthus.ambeth.testutil;

import de.osthus.ambeth.ioc.IServiceContext;

public class InformationBusWithPersistenceTestSetup extends IocTestSetup
{
	public InformationBusWithPersistenceTestSetup(IServiceContext testClassLevelContext, IServiceContext beanContext)
	{
		super(testClassLevelContext, beanContext);
	}
}
