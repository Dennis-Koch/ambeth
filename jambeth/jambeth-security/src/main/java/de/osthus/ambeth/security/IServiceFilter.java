package de.osthus.ambeth.security;

import java.lang.reflect.Method;

import de.osthus.ambeth.model.ISecurityScope;

public interface IServiceFilter
{
	CallPermission checkCallPermissionOnService(Method method, Object[] arguments, SecurityContextType securityContextType, IAuthorization authorization,
			ISecurityScope[] securityScopes);
}
