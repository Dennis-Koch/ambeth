package de.osthus.ambeth.ioc;

import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.log.Logger;
import de.osthus.ambeth.rest.AuthenticationHolder;
import de.osthus.ambeth.rest.IAuthenticationHolder;
import de.osthus.ambeth.rest.RESTClientServiceFactory;
import de.osthus.ambeth.service.IClientServiceFactory;

public class RESTBootstrapModule implements IInitializingModule
{
	@LogInstance
	private Logger Log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory)
	{
		beanContextFactory.registerBean("clientServiceFactory", RESTClientServiceFactory.class).autowireable(IClientServiceFactory.class);
		beanContextFactory.registerBean("authentificationHolder", AuthenticationHolder.class).autowireable(IAuthenticationHolder.class);
	}
}