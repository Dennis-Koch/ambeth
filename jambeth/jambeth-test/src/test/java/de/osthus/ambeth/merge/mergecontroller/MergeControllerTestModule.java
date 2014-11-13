package de.osthus.ambeth.merge.mergecontroller;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public class MergeControllerTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(ParentService.class).autowireable(IParentService.class);
	}
}
