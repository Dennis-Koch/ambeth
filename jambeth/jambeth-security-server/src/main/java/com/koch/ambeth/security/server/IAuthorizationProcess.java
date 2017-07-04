package com.koch.ambeth.security.server;

import com.koch.ambeth.security.SecurityContextType;

public interface IAuthorizationProcess {

	void ensureAuthorization(SecurityContextType securityContextType);
}