package de.osthus.ambeth.query.squery.ioc;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.BootstrapModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.query.squery.service.IPersonService;
import de.osthus.ambeth.query.squery.service.PersonService;

@BootstrapModule
public class SqueryIocModule implements IInitializingModule
{

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(PersonService.class).autowireable(IPersonService.class);
	}
}
