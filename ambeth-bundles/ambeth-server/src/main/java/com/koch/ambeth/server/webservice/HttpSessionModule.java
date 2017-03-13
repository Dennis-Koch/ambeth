package com.koch.ambeth.server.webservice;

import javax.servlet.http.HttpSession;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

public class HttpSessionModule implements IInitializingModule
{

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(HttpSessionBean.class).ignoreProperties("CurrentHttpSession")
				.autowireable(HttpSession.class, IHttpSessionProvider.class);

	}

}
