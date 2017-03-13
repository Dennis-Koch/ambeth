package com.koch.ambeth.server.helloworld.security;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.security.model.IPassword;
import com.koch.ambeth.security.model.IUser;
import com.koch.ambeth.security.server.IPasswordUtil;
import com.koch.ambeth.security.server.ISignatureUtil;
import com.koch.ambeth.security.server.IUserResolver;

public class HelloworldUserResolver implements IUserResolver
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IPasswordUtil passwordUtil;

	@Autowired
	protected ISignatureUtil signatureUtil;

	@Override
	public IUser resolveUserBySID(String sid)
	{
		PojoUser user = new PojoUser(sid);
		user.setPassword(createInMemoryPasswordBySID(user, sid));
		return user;
	}

	protected IPassword createInMemoryPasswordBySID(IUser user, String sid)
	{
		char[] clearTextPassword = sid.toCharArray();
		passwordUtil.assignNewPassword(clearTextPassword, user, null);
		signatureUtil.generateNewSignature(user.getSignature(), clearTextPassword);
		return user.getPassword();
	}
}
