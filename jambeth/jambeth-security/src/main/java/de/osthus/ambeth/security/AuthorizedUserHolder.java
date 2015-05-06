package de.osthus.ambeth.security;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.Forkable;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.SensitiveThreadLocal;

public class AuthorizedUserHolder implements IAuthorizedUserHolder, IThreadLocalCleanupBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Forkable
	protected final ThreadLocal<String> authorizedUserTL = new SensitiveThreadLocal<String>();

	@Override
	public void cleanupThreadLocal()
	{
		if (authorizedUserTL.get() != null)
		{
			throw new IllegalStateException("At this point the thread-local connection has to be already cleaned up gracefully");
		}
	}

	@Override
	public String getAuthorizedUserSID()
	{
		String authorizedUser = authorizedUserTL.get();
		if (authorizedUser != null)
		{
			return authorizedUser;
		}
		ISecurityContext context = securityContextHolder.getContext();
		IAuthorization authorization = context != null ? context.getAuthorization() : null;
		return authorization != null ? authorization.getSID() : null;
	}

	@Override
	public void setAuthorizedUserSID(String sid)
	{
		authorizedUserTL.set(sid);
	}
}
