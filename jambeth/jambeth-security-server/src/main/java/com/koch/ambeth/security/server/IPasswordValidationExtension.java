package com.koch.ambeth.security.server;

public interface IPasswordValidationExtension
{
	CharSequence validatePassword(char[] clearTextPassword);
}
