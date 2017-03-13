package com.koch.ambeth.merge.orihelper;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

public class ORIHelperTestModule implements IInitializingModule
{

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(ORIHelperTestServiceImpl.class).autowireable(ORIHelperTestService.class);
	}

}
