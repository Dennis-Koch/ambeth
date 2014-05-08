package de.osthus.ambeth.security;

public interface IAuthenticationManager
{
	IAuthentication authenticate(IAuthentication authentication) throws AuthenticationException;
}
