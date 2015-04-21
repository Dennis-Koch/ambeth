package de.osthus.ambeth.security.auth;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.security.IAuthentication;
import de.osthus.ambeth.security.IAuthenticationResult;

public interface IAuthenticationResultCache
{

	IAuthenticationResult resolveAuthenticationResult(IAuthentication authentication);

	void cacheAuthenticationResult(IAuthentication authentication, IAuthenticationResult authenticationResult);

}