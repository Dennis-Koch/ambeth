package de.osthus.ambeth.webservice;

import javax.servlet.http.HttpSession;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public class HttpSessionModule implements IInitializingModule
{

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(HttpSessionBean.class).ignoreProperties("CurrentHttpSession")
				.autowireable(HttpSession.class, IHttpSessionProvider.class);

	}

}
