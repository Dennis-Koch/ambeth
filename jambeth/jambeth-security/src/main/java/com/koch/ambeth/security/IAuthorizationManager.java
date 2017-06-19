package com.koch.ambeth.security;

/*-
 * #%L
 * jambeth-security
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

import com.koch.ambeth.service.model.ISecurityScope;

/**
 * Extension which needs to be implemented in order to provide for Ambeth Security the functionality
 * to authorize an authenticated user by his sid against any sophisticated logic. In most cases this
 * means that roles and permissions are resolved from an LDAP or a persisted entity model to fill
 * the created instance of {@link IAuthorization}.
 */
public interface IAuthorizationManager {
	/**
	 * Executes the authorization step: That is that the user is already authenticated (so his
	 * identity is verified) but now all permissions to services and data need to be evaluated.
	 *
	 * @param frameworkSid
	 *          The user's sid used by the security framework. It is implementation dependent whether
	 *          it is equivalent to the SID of {@link IAuthenticationResult#getSID()}. It's value is
	 *          provided internally by using an optional component implementing
	 *          {@link ISidHelper#convertOperatingSystemSidToFrameworkSid(String)}. If no ISIDHelper
	 *          is specified the frameworkSid is the same.
	 * @param securityScopes
	 *          The 'workflow-roles' the user currently impersonates which my influence the evaluated
	 *          result
	 * @param authenticationResult
	 *          The result of the authentication step
	 * @return The result of the authorization step. It may return null which may lead in the further
	 *         processing logic to a {@link InvalidUserException}
	 * 
	 * @see {@link SecurityFilterInterceptor}
	 */
	IAuthorization authorize(String frameworkSid, ISecurityScope[] securityScopes,
			IAuthenticationResult authenticationResult);
}