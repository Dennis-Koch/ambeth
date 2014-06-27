package de.osthus.ambeth.security;

import java.lang.reflect.Method;

import de.osthus.ambeth.model.ISecurityScope;

public interface IAuthorization
{
	String getSID();

	boolean isValid();

	ISecurityScope[] getSecurityScopes();

	CallPermission getCallPermission(Method serviceOperation, ISecurityScope[] securityScopes);

	boolean hasActionPermission(String actionPermissionName, ISecurityScope[] securityScopes);
}
