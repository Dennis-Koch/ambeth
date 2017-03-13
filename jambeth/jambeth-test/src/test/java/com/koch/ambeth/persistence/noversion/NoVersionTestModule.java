package com.koch.ambeth.persistence.noversion;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.persistence.noversion.models.INoVersionService;
import com.koch.ambeth.persistence.noversion.models.NoVersionService;

public class NoVersionTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAutowireableBean(INoVersionService.class, NoVersionService.class);
	}
}
