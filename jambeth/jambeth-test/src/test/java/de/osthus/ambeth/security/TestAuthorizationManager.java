package de.osthus.ambeth.security;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.ISecurityScope;

public class TestAuthorizationManager implements IAuthorizationManager
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public IAuthorization authorize(final String sid, final ISecurityScope[] securityScopes)
	{
		return new DefaultAuthorization(sid, securityScopes, CallPermission.ALLOWED);
	}
}
