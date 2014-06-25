package de.osthus.ambeth.security;

import de.osthus.ambeth.model.ISecurityScope;

public interface IAuthorizationManager
{
	IAuthorization authorize(String sid, ISecurityScope[] securityScopes);
}
