package com.koch.ambeth.security;

import com.koch.ambeth.service.model.ISecurityScope;

public interface IAuthorizationManager
{
	IAuthorization authorize(String sid, ISecurityScope[] securityScopes, IAuthenticationResult authenticationResult);
}
