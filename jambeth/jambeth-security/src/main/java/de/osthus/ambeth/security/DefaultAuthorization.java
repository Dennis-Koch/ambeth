package de.osthus.ambeth.security;

import java.lang.reflect.Method;

import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.model.ITypePrivilege;
import de.osthus.ambeth.privilege.model.impl.SkipAllTypePrivilege;

public class DefaultAuthorization implements IAuthorization
{
	private final ISecurityScope[] securityScopes;

	private final String sid;

	private final CallPermission callPermission;

	private final long authorizationTime;

	private final IAuthenticationResult authenticationResult;

	public DefaultAuthorization(String sid, ISecurityScope[] securityScopes, CallPermission callPermission, long authorizationTime,
			IAuthenticationResult authenticationResult)
	{
		this.sid = sid;
		this.securityScopes = securityScopes;
		this.callPermission = callPermission;
		this.authorizationTime = authorizationTime;
		this.authenticationResult = authenticationResult;
	}

	@Override
	public long getAuthorizationTime()
	{
		return authorizationTime;
	}

	@Override
	public IAuthenticationResult getAuthenticationResult()
	{
		return authenticationResult;
	}

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
	public ITypePrivilege getEntityTypePrivilege(Class<?> entityType, ISecurityScope[] securityScopes)
	{
		return SkipAllTypePrivilege.INSTANCE;
	}

	@Override
	public CallPermission getCallPermission(Method serviceOperation, ISecurityScope[] securityScopes)
	{
		return callPermission;
	}
}
