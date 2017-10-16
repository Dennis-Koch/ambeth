package com.koch.ambeth.security;

public interface IAuthorizationExceptionFactory {
	RuntimeException createAuthorizationException(IAuthentication authentication,
			IAuthorization authorization);
}
