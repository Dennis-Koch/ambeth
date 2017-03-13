package com.koch.ambeth.security.server.auth;

import com.koch.ambeth.security.IAuthentication;
import com.koch.ambeth.security.IAuthenticationResult;

public interface IAuthenticationResultCache
{

	IAuthenticationResult resolveAuthenticationResult(IAuthentication authentication);

	void cacheAuthenticationResult(IAuthentication authentication, IAuthenticationResult authenticationResult);

}