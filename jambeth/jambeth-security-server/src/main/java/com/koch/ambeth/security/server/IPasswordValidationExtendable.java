package com.koch.ambeth.security.server;


public interface IPasswordValidationExtendable
{
	void registerPasswordValidationExtension(IPasswordValidationExtension passwordValidationExtension);

	void unregisterPasswordValidationExtension(IPasswordValidationExtension passwordValidationExtension);
}
