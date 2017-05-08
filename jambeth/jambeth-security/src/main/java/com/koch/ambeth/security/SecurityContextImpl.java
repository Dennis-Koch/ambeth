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

public class SecurityContextImpl implements ISecurityContext {
	protected IAuthentication authentication;

	protected IAuthorization authorization;

	protected final SecurityContextHolder securityContextHolder;

	public SecurityContextImpl(SecurityContextHolder securityContextHolder) {
		this.securityContextHolder = securityContextHolder;
	}

	@Override
	public void setAuthentication(IAuthentication authentication) {
		this.authentication = authentication;
	}

	@Override
	public IAuthentication getAuthentication() {
		return authentication;
	}

	@Override
	public void setAuthorization(IAuthorization authorization) {
		this.authorization = authorization;
		securityContextHolder.notifyAuthorizationChangeListeners(authorization);
	}

	@Override
	public IAuthorization getAuthorization() {
		return authorization;
	}

}
