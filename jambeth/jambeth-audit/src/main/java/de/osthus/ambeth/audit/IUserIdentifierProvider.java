package de.osthus.ambeth.audit;

import de.osthus.ambeth.security.model.IUser;

public interface IUserIdentifierProvider
{
	String getUserIdentifier(IUser user);
}
