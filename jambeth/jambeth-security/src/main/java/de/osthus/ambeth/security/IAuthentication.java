package de.osthus.ambeth.security;

public interface IAuthentication
{
	String getUserName();

	char[] getPassword();

	PasswordType getType();
}
