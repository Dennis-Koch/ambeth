package de.osthus.ambeth.security;

import de.osthus.ambeth.model.ISecurityScope;

public interface ISecurityScopeProvider
{
	ISecurityScope[] getSecurityScopes();

	void setSecurityScopes(ISecurityScope[] securityScopes);

	IUserHandle getUserHandle();

	void setUserHandle(IUserHandle userHandle);
}