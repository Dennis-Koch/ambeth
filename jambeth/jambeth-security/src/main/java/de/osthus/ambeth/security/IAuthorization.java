package de.osthus.ambeth.security;

import java.lang.reflect.Method;

import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.model.ITypePrivilege;

public interface IAuthorization
{
	long getAuthorizationTime();

	String getSID();

	boolean isValid();

	IAuthenticationResult getAuthenticationResult();

	ISecurityScope[] getSecurityScopes();

	CallPermission getCallPermission(Method serviceOperation, ISecurityScope[] securityScopes);

	boolean hasActionPermission(String actionPermissionName, ISecurityScope[] securityScopes);

	ITypePrivilege getEntityTypePrivilege(Class<?> entityType, ISecurityScope[] securityScopes);
}
