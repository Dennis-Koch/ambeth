package de.osthus.ambeth.security;

import de.osthus.ambeth.security.model.IPassword;

public interface IPasswordResolver
{
	IPassword resolvePassword(IAuthentication authentication);
}
