package de.osthus.ambeth.security;

public interface ICheckPasswordResult
{
	boolean isPasswordCorrect();

	boolean isChangePasswordRecommended();

	boolean isRehashPasswordRecommended();
}
