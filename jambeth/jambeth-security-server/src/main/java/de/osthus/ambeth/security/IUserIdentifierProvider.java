package de.osthus.ambeth.security;

import de.osthus.ambeth.security.model.IUser;

public interface IUserIdentifierProvider
{
	String getSID(IUser user);

	boolean isActive(IUser user);

	String getPropertyNameOfSID();
}
