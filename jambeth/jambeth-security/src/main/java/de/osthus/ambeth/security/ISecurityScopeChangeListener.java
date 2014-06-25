package de.osthus.ambeth.security;

import de.osthus.ambeth.model.ISecurityScope;

public interface ISecurityScopeChangeListener
{
	void securityScopeChanged(IAuthorization authorization, ISecurityScope[] securityScopes);
}