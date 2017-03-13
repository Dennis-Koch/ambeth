package com.koch.ambeth.server.helloworld.security;

import java.util.regex.Pattern;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.security.IAuthenticationResult;
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.IAuthorizationManager;
import com.koch.ambeth.security.IServicePermission;
import com.koch.ambeth.security.PermissionApplyType;
import com.koch.ambeth.security.server.AbstractAuthorization;
import com.koch.ambeth.server.helloworld.service.IHelloWorldService;
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.util.collections.HashMap;

public class HelloWorldAuthorizationManager implements IAuthorizationManager
{
	@LogInstance
	private ILogger log;

	protected final Pattern allowAllPattern = Pattern.compile(".*");

	protected final Pattern denyForbiddenMethodPattern = Pattern.compile(IHelloWorldService.class.getName().replaceAll("\\.", "\\\\.") + "\\.forbiddenMethod");

	@Override
	public IAuthorization authorize(String sid, ISecurityScope[] securityScopes, IAuthenticationResult authenticationResult)
	{
		// Allow all service methods
		final Pattern[] allowPatterns = new Pattern[] { allowAllPattern };

		final Pattern[] denyPatterns = new Pattern[] { denyForbiddenMethodPattern };

		final IServicePermission[] servicePermissions = new IServicePermission[] { new IServicePermission()
		{
			@Override
			public Pattern[] getPatterns()
			{
				return allowPatterns;
			}

			@Override
			public PermissionApplyType getApplyType()
			{
				return PermissionApplyType.ALLOW;
			}

		}, new IServicePermission()
		{
			@Override
			public Pattern[] getPatterns()
			{
				return denyPatterns;
			}

			@Override
			public PermissionApplyType getApplyType()
			{
				return PermissionApplyType.DENY;
			}

		} };

		HashMap<ISecurityScope, IServicePermission[]> servicePermissionMap = new HashMap<ISecurityScope, IServicePermission[]>();
		for (ISecurityScope securityScope : securityScopes)
		{
			servicePermissionMap.put(securityScope, servicePermissions);
		}
		return new AbstractAuthorization(servicePermissionMap, securityScopes, null, null, null, null, System.currentTimeMillis(), authenticationResult)
		{
			@Override
			public boolean isValid()
			{
				return false;
			}

			@Override
			public String getSID()
			{
				return null;
			}
		};
	}
}
