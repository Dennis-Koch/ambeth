package com.koch.ambeth.security;

public interface IAuthenticationResult
{
	String getSID();

	boolean isChangePasswordRecommended();

	boolean isRehashPasswordRecommended();
}
