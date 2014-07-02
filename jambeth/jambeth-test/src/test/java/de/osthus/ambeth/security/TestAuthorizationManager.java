package de.osthus.ambeth.security;

import java.lang.reflect.Method;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.IPropertyPrivilege;

public class TestAuthorizationManager implements IAuthorizationManager
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public IAuthorization authorize(final String sid, final ISecurityScope[] securityScopes)
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
				return securityScopes;
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

			@Override
			public IPrivilege getEntityTypePrivilege(Class<?> entityType, ISecurityScope[] securityScopes)
			{
				return new IPrivilege()
				{
					@Override
					public boolean isCreateAllowed()
					{
						return true;
					}

					@Override
					public boolean isReadAllowed()
					{
						return true;
					}

					@Override
					public boolean isUpdateAllowed()
					{
						return true;
					}

					@Override
					public boolean isDeleteAllowed()
					{
						return true;
					}

					@Override
					public boolean isExecutionAllowed()
					{
						return false;
					}

					@Override
					public String[] getConfiguredPropertyNames()
					{
						return new String[0];
					}

					@Override
					public IPropertyPrivilege getPropertyPrivilege(String propertyName)
					{
						return null;
					}

				};
			}
		};
	}
}
