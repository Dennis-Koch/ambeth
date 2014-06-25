package de.osthus.ambeth.security;

import de.osthus.ambeth.model.ISecurityScope;

public interface IAuthorization
{
	String getSID();

	boolean isValid();

	ISecurityScope[] getSecurityScopes();

	IServicePermission[] getServicePermissions(ISecurityScope[] securityScopes);

	boolean hasActionPermission(String actionPermissionName, ISecurityScope[] securityScopes);
}
