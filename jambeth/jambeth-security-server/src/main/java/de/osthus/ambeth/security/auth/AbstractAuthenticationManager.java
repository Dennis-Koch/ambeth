package de.osthus.ambeth.security.auth;

import de.osthus.ambeth.config.IocConfigurationConstants;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.security.AuthenticationException;
import de.osthus.ambeth.security.IAuthentication;
import de.osthus.ambeth.security.IAuthenticationManager;

public abstract class AbstractAuthenticationManager implements IAuthenticationManager
{
	@Property(name = IocConfigurationConstants.DebugModeActive, defaultValue = "false")
	protected boolean debugModeActive;

	protected AuthenticationException createAuthenticationException(IAuthentication authentication)
	{
		// flush the stacktrace so that it can not be reconstructed whether the user existed or why specifically the authentication failed
		// due to security reasons because the source code and its knowledge around it is considered unsafe
		AuthenticationException e = new AuthenticationException("User '" + authentication.getUserName() + "' not found or credentials not valid");
		if (!debugModeActive)
		{
			e.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
		}
		return e;
	}
}
