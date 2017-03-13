package com.koch.ambeth.security.server.auth;

import com.koch.ambeth.ioc.config.IocConfigurationConstants;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.security.AuthenticationException;
import com.koch.ambeth.security.IAuthentication;
import com.koch.ambeth.security.IAuthenticationManager;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public abstract class AbstractAuthenticationManager implements IAuthenticationManager {
	@Property(name = IocConfigurationConstants.DebugModeActive, defaultValue = "false")
	protected boolean debugModeActive;

	protected AuthenticationException createAuthenticationException(IAuthentication authentication) {
		// flush the stacktrace so that it can not be reconstructed whether the user existed or why
		// specifically the authentication failed
		// due to security reasons because the source code and its knowledge around it is considered
		// unsafe
		AuthenticationException e = new AuthenticationException(
				"User '" + authentication.getUserName() + "' not found or credentials not valid");
		if (!debugModeActive) {
			e.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
		}
		return e;
	}
}
