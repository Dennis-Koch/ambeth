package de.osthus.ambeth.security;

import java.lang.reflect.Method;

import de.osthus.ambeth.security.SecurityContext.SecurityContextType;

public interface ISecurityManager
{
	void checkMethodAccess(Method method, Object[] arguments, SecurityContextType securityContextType, IUserHandle userHandle);

	<T> T filterValue(T value);
}
