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
	public IAuthorization authorize(String sid, ISecurityScope[] securityScopes, IAuthenticationResult authenticationResult)
	{
		return new DefaultAuthorization(sid, securityScopes, CallPermission.ALLOWED, System.currentTimeMillis(), authenticationResult);
	}
}
