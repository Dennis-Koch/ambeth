package com.koch.ambeth.security.server;

import com.koch.ambeth.security.IAuthentication;
import com.koch.ambeth.security.IAuthorization;

public interface IAuthorizationExceptionFactory
{
	Throwable createAuthorizationException(IAuthentication authentication, IAuthorization authorization);
}
