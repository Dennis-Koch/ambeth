package de.osthus.ambeth.security;

import de.osthus.ambeth.ioc.DefaultExtendableContainer;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.SensitiveThreadLocal;

public final class SecurityContextHolder implements IAuthorizationChangeListenerExtendable
{

	protected final DefaultExtendableContainer<IAuthorizationChangeListener> authorizationChangeListeners = new DefaultExtendableContainer<IAuthorizationChangeListener>(
			IAuthorizationChangeListener.class, "authorizationChangeListener");

	private class SecurityContextImpl implements ISecurityContext
	{
		protected IAuthentication authentication;

		protected IAuthorization authorization;

		@Override
		public void setAuthentication(IAuthentication authentication)
		{
			this.authentication = authentication;
		}

		@Override
		public IAuthentication getAuthentication()
		{
			return authentication;
		}

		@Override
		public void setAuthorization(IAuthorization authorization)
		{
			this.authorization = authorization;
			notifyAuthorizationChangeListeners(authorization);
		}

		@Override
		public IAuthorization getAuthorization()
		{
			return authorization;
		}

	}

	protected void notifyAuthorizationChangeListeners(IAuthorization authorization)
	{
		for (IAuthorizationChangeListener authorizationChangeListener : authorizationChangeListeners.getExtensions())
		{
			authorizationChangeListener.authorizationChanged(authorization);
		}
	}

	@Override
	public void registerAuthorizationChangeListener(IAuthorizationChangeListener authorizationChangeListener)
	{
		authorizationChangeListeners.register(authorizationChangeListener);
	}

	@Override
	public void unregisterAuthorizationChangeListener(IAuthorizationChangeListener authorizationChangeListener)
	{
		authorizationChangeListeners.unregister(authorizationChangeListener);
	}

	protected final ThreadLocal<ISecurityContext> contextTL = new SensitiveThreadLocal<ISecurityContext>();

	public ISecurityContext getContext()
	{
		return contextTL.get();
	}

	public ISecurityContext getCreateContext()
	{
		ISecurityContext securityContext = getContext();
		if (securityContext == null)
		{
			securityContext = new SecurityContextImpl();
			contextTL.set(securityContext);
		}
		return securityContext;
	}

	public void clearContext()
	{
		ISecurityContext securityContext = contextTL.get();
		if (securityContext != null)
		{
			securityContext.setAuthentication(null);
			contextTL.remove();
		}
	}

	public <R> R setScopedAuthentication(IAuthentication authentication, IResultingBackgroundWorkerDelegate<R> runnableScope) throws Throwable
	{
		ISecurityContext securityContext = getContext();
		boolean created = false;
		if (securityContext == null)
		{
			securityContext = new SecurityContextImpl();
			contextTL.set(securityContext);
			created = true;
		}
		IAuthorization oldAuthorization = securityContext.getAuthorization();
		IAuthentication oldAuthentication = securityContext.getAuthentication();
		try
		{
			if (oldAuthentication == authentication)
			{
				return runnableScope.invoke();
			}
			try
			{
				securityContext.setAuthentication(authentication);
				securityContext.setAuthorization(null);
				return runnableScope.invoke();
			}
			finally
			{
				securityContext.setAuthentication(oldAuthentication);
				securityContext.setAuthorization(oldAuthorization);
			}
		}
		finally
		{
			if (created)
			{
				contextTL.remove();
			}
		}
	}
}
