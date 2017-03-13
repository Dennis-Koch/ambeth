package com.koch.ambeth.security.server;

import com.koch.ambeth.security.model.IUser;

public interface IUserIdentifierProvider
{
	String getSID(IUser user);

	boolean isActive(IUser user);

	String getPropertyNameOfSID();
}
