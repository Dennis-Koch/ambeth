package de.osthus.ambeth.security;

public interface IAuthenticationResult
{
	String getUserName();

	boolean isChangePasswordRecommended();

	boolean isRehashPasswordRecommended();
}
