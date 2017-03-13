package com.koch.ambeth.persistence.jdbc.compositeid;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.persistence.jdbc.compositeid.models.CompositeIdEntityService;
import com.koch.ambeth.persistence.jdbc.compositeid.models.ICompositeIdEntityService;

public class CompositeIdTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAutowireableBean(ICompositeIdEntityService.class, CompositeIdEntityService.class);
	}
}
