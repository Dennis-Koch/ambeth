package com.koch.ambeth.security;

public interface IAuthenticationManager
{
	IAuthenticationResult authenticate(IAuthentication authentication) throws AuthenticationException;
}
