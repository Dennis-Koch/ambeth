package com.koch.ambeth.security;

public interface IRemoteAuthenticationManager
{
	Object attemptAuthentication(String userName, String userpassword);
}
