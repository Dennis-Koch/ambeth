package de.osthus.ambeth.security;

import java.lang.reflect.Method;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.ISecurityScope;

public class TestAuthorizationManager implements IAuthorizationManager
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public IAuthorization authorize(final String sid, ISecurityScope[] securityScopes)
	{
		return new IAuthorization()
		{
			@Override
			public boolean isValid()
			{
				return true;
			}

			@Override
			public boolean hasActionPermission(String actionPermissionName, ISecurityScope[] securityScopes)
			{
				return true;
			}

			@Override
			public ISecurityScope[] getSecurityScopes()
			{
				return null;
			}

			@Override
			public String getSID()
			{
				return sid;
			}

			@Override
			public CallPermission getCallPermission(Method serviceOperation, ISecurityScope[] securityScopes)
			{
				return CallPermission.ALLOWED;
			}
		};
	}
}
