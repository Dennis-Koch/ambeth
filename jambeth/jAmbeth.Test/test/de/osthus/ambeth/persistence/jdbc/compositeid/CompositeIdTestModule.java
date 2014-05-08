package de.osthus.ambeth.persistence.jdbc.compositeid;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.jdbc.compositeid.models.CompositeIdEntityService;
import de.osthus.ambeth.persistence.jdbc.compositeid.models.ICompositeIdEntityService;

public class CompositeIdTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAutowireableBean(ICompositeIdEntityService.class, CompositeIdEntityService.class);
	}
}
