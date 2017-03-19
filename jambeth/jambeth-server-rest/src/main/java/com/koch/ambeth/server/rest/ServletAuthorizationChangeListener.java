package com.koch.ambeth.server.rest;

import javax.servlet.http.HttpSession;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.IAuthorizationChangeListener;
import com.koch.ambeth.server.webservice.IHttpSessionProvider;

public class ServletAuthorizationChangeListener implements IAuthorizationChangeListener
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Override
	public void authorizationChanged(IAuthorization authorization)
	{
		if (authorization == null)
		{
			return;
		}
		IHttpSessionProvider httpSessionProvider = beanContext.getService(IHttpSessionProvider.class);
		if (httpSessionProvider.getCurrentHttpSession() != null)
		{
			beanContext.getService(HttpSession.class).setAttribute(AmbethServletRequestFilter.ATTRIBUTE_AUTHORIZATION_HANDLE, authorization);
		}
	}
}
