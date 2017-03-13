package com.koch.ambeth.security.server;

public interface ICheckPasswordResult
{
	boolean isPasswordCorrect();

	boolean isChangePasswordRecommended();

	boolean isRehashPasswordRecommended();
}
