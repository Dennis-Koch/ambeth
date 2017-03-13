package com.koch.ambeth.security;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.security.model.IUser;
import com.koch.ambeth.security.server.IUserIdentifierProvider;
import com.koch.ambeth.security.server.IUserResolver;

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
