package de.osthus.ambeth.security;

public interface IRemoteAuthenticationManager
{
	Object attemptAuthentication(String userName, String userpassword);
}
