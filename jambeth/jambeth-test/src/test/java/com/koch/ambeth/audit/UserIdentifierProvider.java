package com.koch.ambeth.audit;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.security.model.IUser;
import com.koch.ambeth.security.server.IUserIdentifierProvider;

public class UserIdentifierProvider implements IUserIdentifierProvider
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public boolean isActive(IUser user)
	{
		return true;
	}

	@Override
	public String getSID(IUser user)
	{
		return ((User) user).getSID();
	}

	@Override
	public String getPropertyNameOfSID()
	{
		return User.SID;
	}
}
