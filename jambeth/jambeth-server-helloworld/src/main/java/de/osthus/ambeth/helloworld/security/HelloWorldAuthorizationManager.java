package de.osthus.ambeth.helloworld.security;

import java.util.regex.Pattern;

import de.osthus.ambeth.helloworld.service.IHelloWorldService;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.security.IAuthorization;
import de.osthus.ambeth.security.IAuthorizationManager;
import de.osthus.ambeth.security.IServicePermission;
import de.osthus.ambeth.security.PermissionApplyType;

public class HelloWorldAuthorizationManager implements IAuthorizationManager
{
	@LogInstance
	private ILogger log;

	protected final Pattern allowAllPattern = Pattern.compile(".*");

	protected final Pattern denyForbiddenMethodPattern = Pattern.compile(IHelloWorldService.class.getName().replaceAll("\\.", "\\\\.") + "\\.forbiddenMethod");

	@Override
	public IAuthorization authorize(final String sid, final ISecurityScope[] securityScopes)
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

		return new IAuthorization()
		{
			@Override
			public boolean isValid()
			{
				return true;
			}

			@Override
			public IServicePermission[] getServicePermissions(ISecurityScope[] securityScopes)
			{
				return servicePermissions;
			}

			@Override
			public String getSID()
			{
				return sid;
			}

			@Override
			public ISecurityScope[] getSecurityScopes()
			{
				return securityScopes;
			}

			@Override
			public boolean hasActionPermission(String actionPermissionName, ISecurityScope[] securityScopes)
			{
				return false;
			}
		};
	}
}
