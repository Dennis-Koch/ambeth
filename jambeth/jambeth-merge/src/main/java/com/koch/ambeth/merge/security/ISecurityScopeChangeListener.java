package com.koch.ambeth.merge.security;

import com.koch.ambeth.service.model.ISecurityScope;

public interface ISecurityScopeChangeListener
{
	void securityScopeChanged(ISecurityScope[] securityScopes);
}