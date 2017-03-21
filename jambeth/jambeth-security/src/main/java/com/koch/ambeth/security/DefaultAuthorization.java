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

import java.lang.reflect.Method;

import com.koch.ambeth.security.privilege.model.ITypePrivilege;
import com.koch.ambeth.security.privilege.model.impl.SkipAllTypePrivilege;
import com.koch.ambeth.service.model.ISecurityScope;

public class DefaultAuthorization implements IAuthorization {
	private final ISecurityScope[] securityScopes;

	private final String sid;

	private final CallPermission callPermission;

	private final long authorizationTime;

	private final IAuthenticationResult authenticationResult;

	public DefaultAuthorization(String sid, ISecurityScope[] securityScopes,
			CallPermission callPermission, long authorizationTime,
			IAuthenticationResult authenticationResult) {
		this.sid = sid;
		this.securityScopes = securityScopes;
		this.callPermission = callPermission;
		this.authorizationTime = authorizationTime;
		this.authenticationResult = authenticationResult;
	}

	@Override
	public long getAuthorizationTime() {
		return authorizationTime;
	}

	@Override
	public IAuthenticationResult getAuthenticationResult() {
		return authenticationResult;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public boolean hasActionPermission(String actionPermissionName, ISecurityScope[] securityScopes) {
		return true;
	}

	@Override
	public ISecurityScope[] getSecurityScopes() {
		return securityScopes;
	}

	@Override
	public String getSID() {
		return sid;
	}

	@Override
	public ITypePrivilege getEntityTypePrivilege(Class<?> entityType,
			ISecurityScope[] securityScopes) {
		return SkipAllTypePrivilege.INSTANCE;
	}

	@Override
	public CallPermission getCallPermission(Method serviceOperation,
			ISecurityScope[] securityScopes) {
		return callPermission;
	}
}
