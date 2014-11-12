package de.osthus.ambeth.security;

import de.osthus.ambeth.ioc.DefaultExtendableContainer;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.SensitiveThreadLocal;

public class SecurityContextHolder implements IAuthorizationChangeListenerExtendable, ISecurityContextHolder, IThreadLocalCleanupBean
{
	protected final DefaultExtendableContainer<IAuthorizationChangeListener> authorizationChangeListeners = new DefaultExtendableContainer<IAuthorizationChangeListener>(
			IAuthorizationChangeListener.class, "authorizationChangeListener");

	protected final ThreadLocal<ISecurityContext> contextTL = new SensitiveThreadLocal<ISecurityContext>();

	protected void notifyAuthorizationChangeListeners(IAuthorization authorization)
	{
		for (IAuthorizationChangeListener authorizationChangeListener : authorizationChangeListeners.getExtensions())
		{
			authorizationChangeListener.authorizationChanged(authorization);
		}
	}

	@Override
	public void cleanupThreadLocal()
	{
		clearContext();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.security.ISecurityContextHolder#getContext()
	 */
	@Override
	public ISecurityContext getContext()
	{
		return contextTL.get();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.security.ISecurityContextHolder#getCreateContext()
	 */
	@Override
	public ISecurityContext getCreateContext()
	{
		ISecurityContext securityContext = getContext();
		if (securityContext == null)
		{
			securityContext = new SecurityContextImpl(this);
			contextTL.set(securityContext);
		}
		return securityContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.security.ISecurityContextHolder#clearContext()
	 */
	@Override
	public void clearContext()
	{
		ISecurityContext securityContext = contextTL.get();
		if (securityContext != null)
		{
			securityContext.setAuthentication(null);
			securityContext.setAuthorization(null);
			contextTL.remove();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.security.ISecurityContextHolder#setScopedAuthentication(de.osthus.ambeth.security.IAuthentication,
	 * de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate)
	 */
	@Override
	public <R> R setScopedAuthentication(IAuthentication authentication, IResultingBackgroundWorkerDelegate<R> runnableScope) throws Throwable
	{
		ISecurityContext securityContext = getContext();
		boolean created = false;
		if (securityContext == null)
		{
			securityContext = getCreateContext();
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
				clearContext();
			}
		}
	}
}
