package de.osthus.ambeth.query.isin;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public class QueryIsInMassdataTestModule implements IInitializingModule
{

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(ChildService.class).autowireable(IChildService.class);
	}

}