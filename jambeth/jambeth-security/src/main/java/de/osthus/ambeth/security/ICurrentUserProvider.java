package de.osthus.ambeth.security;

import de.osthus.ambeth.security.model.IUser;

public interface ICurrentUserProvider
{
	IUser getCurrentUser();
}
