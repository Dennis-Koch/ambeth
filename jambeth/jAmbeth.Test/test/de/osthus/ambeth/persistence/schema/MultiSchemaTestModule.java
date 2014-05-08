package de.osthus.ambeth.persistence.schema;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.schema.models.IParentAService;
import de.osthus.ambeth.persistence.schema.models.IParentBService;
import de.osthus.ambeth.persistence.schema.models.ParentAService;
import de.osthus.ambeth.persistence.schema.models.ParentBService;

public class MultiSchemaTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAutowireableBean(IParentAService.class, ParentAService.class);
		beanContextFactory.registerAutowireableBean(IParentBService.class, ParentBService.class);
	}
}
