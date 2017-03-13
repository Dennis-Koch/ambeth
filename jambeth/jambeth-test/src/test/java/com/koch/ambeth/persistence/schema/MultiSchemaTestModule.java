package com.koch.ambeth.persistence.schema;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.persistence.schema.models.IParentAService;
import com.koch.ambeth.persistence.schema.models.IParentBService;
import com.koch.ambeth.persistence.schema.models.ParentAService;
import com.koch.ambeth.persistence.schema.models.ParentBService;

public class MultiSchemaTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAutowireableBean(IParentAService.class, ParentAService.class);
		beanContextFactory.registerAutowireableBean(IParentBService.class, ParentBService.class);
	}
}
