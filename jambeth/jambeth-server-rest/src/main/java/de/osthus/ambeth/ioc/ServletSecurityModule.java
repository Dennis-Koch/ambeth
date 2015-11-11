package de.osthus.ambeth.ioc;

import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.security.IAuthorizationChangeListenerExtendable;
import de.osthus.ambeth.webservice.ServletAuthorizationChangeListener;

public class ServletSecurityModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		IBeanConfiguration bean = beanContextFactory.registerBean(ServletAuthorizationChangeListener.class);
		beanContextFactory.link(bean).to(IAuthorizationChangeListenerExtendable.class).optional();
	}
}
