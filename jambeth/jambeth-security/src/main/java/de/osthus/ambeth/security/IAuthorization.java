package de.osthus.ambeth.security;

import java.lang.reflect.Method;

import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.model.IPrivilege;

public interface IAuthorization
{
	String getSID();

	boolean isValid();

	ISecurityScope[] getSecurityScopes();

	CallPermission getCallPermission(Method serviceOperation, ISecurityScope[] securityScopes);

	boolean hasActionPermission(String actionPermissionName, ISecurityScope[] securityScopes);

	IPrivilege getEntityTypePrivilege(Class<?> entityType, ISecurityScope[] securityScopes);
}
