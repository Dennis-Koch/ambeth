package de.osthus.ambeth.security;

import java.lang.reflect.Method;

import de.osthus.ambeth.security.SecurityContext.SecurityContextType;

public interface IServiceFilter
{
	CallPermission checkCallPermissionOnService(Method method, Object[] arguments, SecurityContextType securityContextType, IUserHandle userHandle);
}