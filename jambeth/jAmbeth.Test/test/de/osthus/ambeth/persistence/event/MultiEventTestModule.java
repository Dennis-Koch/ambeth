package de.osthus.ambeth.persistence.event;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public class MultiEventTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAnonymousBean(MultiEventService.class).autowireable(IMultiEventService.class);
	}
}
