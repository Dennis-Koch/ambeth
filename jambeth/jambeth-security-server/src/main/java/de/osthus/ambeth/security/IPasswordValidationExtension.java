package de.osthus.ambeth.security;

public interface IPasswordValidationExtension
{
	CharSequence validatePassword(char[] clearTextPassword);
}
