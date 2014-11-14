package de.osthus.ambeth.security;

public interface IAuthorizationExceptionFactory
{
	Throwable createAuthorizationException(IAuthentication authentication, IAuthorization authorization);
}
