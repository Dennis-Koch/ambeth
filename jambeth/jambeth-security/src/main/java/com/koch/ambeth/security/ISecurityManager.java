package com.koch.ambeth.security;

import java.lang.reflect.Method;

public interface ISecurityManager
{
	void checkMethodAccess(Method method, Object[] arguments, SecurityContextType securityContextType, IAuthorization authorization);

	<T> T filterValue(T value);
}
