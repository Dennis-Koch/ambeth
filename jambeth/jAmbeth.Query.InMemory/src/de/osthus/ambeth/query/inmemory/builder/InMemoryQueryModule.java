package de.osthus.ambeth.query.inmemory.builder;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.query.IQueryBuilderFactory;

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
