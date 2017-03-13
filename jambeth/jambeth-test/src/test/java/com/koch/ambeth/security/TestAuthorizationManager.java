package com.koch.ambeth.security;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.security.CallPermission;
import com.koch.ambeth.security.DefaultAuthorization;
import com.koch.ambeth.security.IAuthenticationResult;
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.IAuthorizationManager;
import com.koch.ambeth.service.model.ISecurityScope;

public class TestAuthorizationManager implements IAuthorizationManager
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public IAuthorization authorize(String sid, ISecurityScope[] securityScopes, IAuthenticationResult authenticationResult)
	{
		return new DefaultAuthorization(sid, securityScopes, CallPermission.ALLOWED, System.currentTimeMillis(), authenticationResult);
	}
}
