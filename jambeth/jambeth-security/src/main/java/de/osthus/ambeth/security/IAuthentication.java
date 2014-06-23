package de.osthus.ambeth.security;

public interface IAuthentication
{
	public static enum PasswordType
	{
		PLAIN, MD5, SHA1;
	}

	String getUserName();

	byte[] getPassword();

	PasswordType getType();
}
