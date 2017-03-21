package com.koch.ambeth.security.server.auth;

/*-
 * #%L
 * jambeth-security-server
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

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
