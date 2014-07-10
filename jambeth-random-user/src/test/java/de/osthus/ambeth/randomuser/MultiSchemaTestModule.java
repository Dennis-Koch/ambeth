package de.osthus.ambeth.randomuser;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.randomuser.models.IParentAService;
import de.osthus.ambeth.randomuser.models.IParentBService;
import de.osthus.ambeth.randomuser.models.ParentAService;
import de.osthus.ambeth.randomuser.models.ParentBService;

public class MultiSchemaTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAutowireableBean(IParentAService.class, ParentAService.class);
		beanContextFactory.registerAutowireableBean(IParentBService.class, ParentBService.class);
	}
}
