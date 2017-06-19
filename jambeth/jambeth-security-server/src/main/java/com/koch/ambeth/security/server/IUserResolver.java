package com.koch.ambeth.security.server;

import com.koch.ambeth.security.IAuthentication;
import com.koch.ambeth.security.IAuthenticationManager;

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

import com.koch.ambeth.security.model.IUser;
import com.koch.ambeth.security.server.auth.EmbeddedAuthenticationManager;
import com.koch.ambeth.security.server.config.SecurityServerConfigurationConstants;
import com.koch.ambeth.security.server.ioc.SecurityServerModule;

/**
 * Provides discrete query and retrieval functionality for managed {@link IUser} handles. The
 * execution may involve e.g. external LDAP requests or just a local database query. It is used by
 * the {@link EmbeddedAuthenticationManager} which is chosen as the default
 * {@link IAuthenticationManager} if nothing else is specified explicitly via
 * 'security.authmanager.type'.
 *
 * @see EmbeddedAuthenticationManager
 * @see IAuthenticationManager#authenticate(IAuthentication)
 * @see SecurityServerConfigurationConstants#AuthenticationManagerType
 * @see SecurityServerModule#afterPropertiesSet(com.koch.ambeth.ioc.factory.IBeanContextFactory)
 */
public interface IUserResolver {
	/**
	 * Resolves the user handle by a given SID (unique identifier). In the default behavior the SID is
	 * the value provided by the {@link IAuthentication#getUserName()} called on the current
	 * authentication passed to {@link IAuthenticationManager#authenticate(IAuthentication)}.
	 *
	 * @param sid
	 *          The sid to look up the user handle for
	 * @return The resolved user handle, may be null if no user handle is mapped to the given sid.
	 */
	IUser resolveUserBySID(String sid);
}
