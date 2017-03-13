package com.koch.ambeth.security;

import java.lang.reflect.Method;

import com.koch.ambeth.security.privilege.model.ITypePrivilege;
import com.koch.ambeth.service.model.ISecurityScope;

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
