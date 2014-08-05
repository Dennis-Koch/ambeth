package de.osthus.ambeth.helloworld.security;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.security.IPasswordUtil;
import de.osthus.ambeth.security.IUserResolver;
import de.osthus.ambeth.security.model.IPassword;
import de.osthus.ambeth.security.model.IUser;

public class HelloworldUserResolver implements IUserResolver
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IPasswordUtil passwordUtil;

	@Override
	public IUser resolveUserBySID(String sid)
	{
		PojoUser user = new PojoUser();
		user.setPassword(createInMemoryPasswordBySID(user, sid));
		return user;
	}

	protected IPassword createInMemoryPasswordBySID(IUser user, String sid)
	{
		IPassword password = new PojoPassword();
		passwordUtil.fillNewPassword(sid.toCharArray(), password, user);
		return password;
	}
}
