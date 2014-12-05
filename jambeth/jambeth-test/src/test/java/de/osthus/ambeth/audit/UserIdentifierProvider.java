package de.osthus.ambeth.audit;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.security.IUserIdentifierProvider;
import de.osthus.ambeth.security.model.IUser;

public class UserIdentifierProvider implements IUserIdentifierProvider
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

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
