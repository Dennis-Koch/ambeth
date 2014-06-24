package de.osthus.ambeth.security;

public interface IAuthenticationManager
{
	IAuthenticationResult authenticate(IAuthentication authentication) throws AuthenticationException;
}
