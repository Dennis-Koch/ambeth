package de.osthus.ambeth.relations;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public class RelationsTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("relationsService", RelationsService.class).autowireable(IRelationsService.class);
	}
}
