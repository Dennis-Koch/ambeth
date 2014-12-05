package de.osthus.ambeth.security;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.security.model.IUser;

public class TestUserResolver implements IUserResolver, IInitializingBean
{
	@Autowired
	protected ICache cache;

	@Autowired
	protected IUserIdentifierProvider userIdentifierProvider;

	protected String propertyNameOfSID;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		propertyNameOfSID = userIdentifierProvider.getPropertyNameOfSID();
	}

	@Override
	public IUser resolveUserBySID(String sid)
	{
		return cache.getObject(IUser.class, propertyNameOfSID, sid);
	}
}
