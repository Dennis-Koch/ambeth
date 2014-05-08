package de.osthus.ambeth.persistence.noversion;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.noversion.models.INoVersionService;
import de.osthus.ambeth.persistence.noversion.models.NoVersionService;

public class NoVersionTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAutowireableBean(INoVersionService.class, NoVersionService.class);
	}
}
