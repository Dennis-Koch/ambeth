package de.osthus.ambeth.security;

import de.osthus.ambeth.ioc.DefaultExtendableContainer;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.Forkable;
import de.osthus.ambeth.ioc.threadlocal.IForkProcessor;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.SensitiveThreadLocal;

public class SecurityContextHolder implements IAuthorizationChangeListenerExtendable, ISecurityContextHolder, IThreadLocalCleanupBean,
		ILightweightSecurityContext
{
	public static class SecurityContextForkProcessor implements IForkProcessor
	{
		@Override
		public Object resolveOriginalValue(Object bean, String fieldName, ThreadLocal<?> fieldValueTL)
		{
			return fieldValueTL.get();
		}

		@Override
		public Object createForkedValue(Object value)
		{
			if (value == null)
			{
				return null;
			}
			SecurityContextImpl original = (SecurityContextImpl) value;
			SecurityContextImpl forkedValue = new SecurityContextImpl(original.securityContextHolder);
			forkedValue.setAuthentication(original.getAuthentication());
			forkedValue.setAuthorization(original.getAuthorization());
			return forkedValue;
		}

		@Override
		public void returnForkedValue(Object value, Object forkedValue)
		{
			// Intended blank
		}
	}

	@Autowired
	protected IAuthenticatedUserHolder authenticatedUserHolder;

	protected final DefaultExtendableContainer<IAuthorizationChangeListener> authorizationChangeListeners = new DefaultExtendableContainer<IAuthorizationChangeListener>(
			IAuthorizationChangeListener.class, "authorizationChangeListener");

	@Forkable(processor = SecurityContextForkProcessor.class)
	protected final ThreadLocal<ISecurityContext> contextTL = new SensitiveThreadLocal<ISecurityContext>();

	protected void notifyAuthorizationChangeListeners(IAuthorization authorization)
	{
		authenticatedUserHolder.setAuthenticatedSID(authorization != null ? authorization.getSID() : null);
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

	@Override
	public boolean isAuthenticated()
	{
		ISecurityContext securityContext = getContext();
		if (securityContext == null)
		{
			return false;
		}
		return securityContext.getAuthorization() != null;
	}
}
