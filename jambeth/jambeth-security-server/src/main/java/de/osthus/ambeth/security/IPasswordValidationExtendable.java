package de.osthus.ambeth.security;


public interface IPasswordValidationExtendable
{
	void registerPasswordValidationExtension(IPasswordValidationExtension passwordValidationExtension);

	void unregisterPasswordValidationExtension(IPasswordValidationExtension passwordValidationExtension);
}
