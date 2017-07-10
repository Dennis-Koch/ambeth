package com.koch.ambeth.security;

import com.koch.ambeth.service.model.ISecurityScope;

public class TestAuthorizationManager implements IAuthorizationManager {
	@Override
	public IAuthorization authorize(String sid, ISecurityScope[] securityScopes,
			IAuthenticationResult authenticationResult) {
		return new DefaultAuthorization(sid, securityScopes, CallPermission.ALLOWED,
				System.currentTimeMillis(), authenticationResult);
	}
}
