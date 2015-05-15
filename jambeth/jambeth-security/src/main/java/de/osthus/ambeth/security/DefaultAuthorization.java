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

	public DefaultAuthorization(String sid, ISecurityScope[] securityScopes, CallPermission callPermission)
	{
		super();
		this.sid = sid;
		this.securityScopes = securityScopes;
		this.callPermission = callPermission;
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
