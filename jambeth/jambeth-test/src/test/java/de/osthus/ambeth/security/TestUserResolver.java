package de.osthus.ambeth.security;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.security.model.IUser;

public class TestUserResolver implements IUserResolver
{
	@Autowired
	protected ICache cache;

	@Override
	public IUser resolveUserBySID(String sid)
	{
		sid = sid.toLowerCase();
		return cache.getObject(User.class, User.SID, sid);
	}
}
