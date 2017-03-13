package com.koch.ambeth.query.inmemory.builder;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.query.IQueryBuilderFactory;

public class InMemoryQueryModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("inMemoryQueryBuilderFactory", InMemoryQueryBuilderFactory.class).autowireable(IQueryBuilderFactory.class);
	}
}
