package de.osthus.ambeth.security;

import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.SensitiveThreadLocal;

public final class SecurityContextHolder
{
	private static class SecurityContextImpl implements ISecurityContext
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
		}

		@Override
		public IAuthorization getAuthorization()
		{
			return authorization;
		}
	}

	protected static final ThreadLocal<ISecurityContext> contextTL = new SensitiveThreadLocal<ISecurityContext>();

	public static ISecurityContext getContext()
	{
		return contextTL.get();
	}

	public static ISecurityContext getCreateContext()
	{
		ISecurityContext securityContext = getContext();
		if (securityContext == null)
		{
			securityContext = new SecurityContextImpl();
			contextTL.set(securityContext);
		}
		return securityContext;
	}

	public static void clearContext()
	{
		ISecurityContext securityContext = contextTL.get();
		if (securityContext != null)
		{
			securityContext.setAuthentication(null);
			contextTL.remove();
		}
	}

	public static <R> R setScopedAuthentication(IAuthentication authentication, IResultingBackgroundWorkerDelegate<R> runnableScope) throws Throwable
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

	private SecurityContextHolder()
	{
		// intended blank
	}
}
