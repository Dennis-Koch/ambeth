package de.osthus.ambeth.webservice;

import javax.servlet.http.HttpSession;

import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.security.IAuthorization;
import de.osthus.ambeth.security.IAuthorizationChangeListener;

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
