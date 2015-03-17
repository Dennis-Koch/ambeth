package de.osthus.ambeth.merge.orihelper;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public class ORIHelperTestModule implements IInitializingModule
{

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(ORIHelperTestServiceImpl.class).autowireable(ORIHelperTestService.class);
	}

}
