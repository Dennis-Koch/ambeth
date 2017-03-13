package com.koch.ambeth.query.squery.ioc;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.BootstrapModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.query.squery.service.IPersonService;
import com.koch.ambeth.query.squery.service.PersonService;

@BootstrapModule
public class SqueryIocModule implements IInitializingModule
{

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(PersonService.class).autowireable(PersonService.class, IPersonService.class);
	}
}
