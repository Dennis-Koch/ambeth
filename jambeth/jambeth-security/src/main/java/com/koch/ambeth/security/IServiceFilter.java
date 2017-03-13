package com.koch.ambeth.security;

import java.lang.reflect.Method;

import com.koch.ambeth.service.model.ISecurityScope;

public interface IServiceFilter
{
	CallPermission checkCallPermissionOnService(Method method, Object[] arguments, SecurityContextType securityContextType, IAuthorization authorization,
			ISecurityScope[] securityScopes);
}
