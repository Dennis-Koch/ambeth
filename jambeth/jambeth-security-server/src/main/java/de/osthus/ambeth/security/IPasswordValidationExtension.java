package de.osthus.ambeth.security;

public interface IPasswordValidationExtension
{
	String validatePassword(char[] clearTextPassword);
}
