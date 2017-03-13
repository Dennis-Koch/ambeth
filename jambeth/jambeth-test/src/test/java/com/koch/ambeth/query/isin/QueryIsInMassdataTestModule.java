package com.koch.ambeth.query.isin;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

public class QueryIsInMassdataTestModule implements IInitializingModule
{

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(ChildService.class).autowireable(IChildService.class);
	}

}