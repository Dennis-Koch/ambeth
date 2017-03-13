package com.koch.ambeth.security;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;

public class AuthenticatedUserHolder implements IAuthenticatedUserHolder, IThreadLocalCleanupBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Forkable
	protected final ThreadLocal<String> authenticatedUserTL = new SensitiveThreadLocal<String>();

	@Override
	public void cleanupThreadLocal()
	{
		if (authenticatedUserTL.get() != null)
		{
			throw new IllegalStateException("At this point the thread-local connection has to be already cleaned up gracefully");
		}
	}

	@Override
	public String getAuthenticatedSID()
	{
		String authorizedUser = authenticatedUserTL.get();
		if (authorizedUser != null)
		{
			return authorizedUser;
		}
		ISecurityContext context = securityContextHolder.getContext();
		IAuthorization authorization = context != null ? context.getAuthorization() : null;
		return authorization != null ? authorization.getSID() : null;
	}

	@Override
	public void setAuthenticatedSID(String sid)
	{
		authenticatedUserTL.set(sid);
	}
}
