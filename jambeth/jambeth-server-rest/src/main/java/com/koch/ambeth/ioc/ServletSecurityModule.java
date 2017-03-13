package com.koch.ambeth.ioc;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.security.IAuthorizationChangeListenerExtendable;
import com.koch.ambeth.webservice.ServletAuthorizationChangeListener;

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
