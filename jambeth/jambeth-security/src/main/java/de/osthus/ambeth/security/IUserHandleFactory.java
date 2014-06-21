package de.osthus.ambeth.security;

import de.osthus.ambeth.model.ISecurityScope;

public interface IUserHandleFactory
{
	IUserHandle createUserHandle(String sid, String password, ISecurityScope[] securityScopes);
}
