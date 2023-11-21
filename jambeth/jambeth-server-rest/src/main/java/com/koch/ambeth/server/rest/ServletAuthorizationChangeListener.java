package com.koch.ambeth.server.rest;

/*-
 * #%L
 * jambeth-server-rest
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

import jakarta.servlet.http.HttpSession;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.security.IAuthentication;
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.IAuthorizationChangeListener;
import com.koch.ambeth.security.ISecurityContext;
import com.koch.ambeth.security.ISecurityContextHolder;
import com.koch.ambeth.server.webservice.IHttpSessionProvider;

public class ServletAuthorizationChangeListener implements IAuthorizationChangeListener {
	@Autowired(optional = true)
	protected IHttpSessionProvider httpSessionProvider;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Override
	public void authorizationChanged(IAuthorization authorization) {
		if (authorization == null || httpSessionProvider == null) {
			return;
		}
		var httpSession = httpSessionProvider.getCurrentHttpSession();
		if (httpSession != null) {
			var securityContext = securityContextHolder.getContext();
			var authentication = securityContext != null ? securityContext.getAuthentication() : null;
			httpSession.setAttribute(AmbethServletAspect.ATTRIBUTE_AUTHENTICATION_HANDLE, authentication);
			httpSession.setAttribute(AmbethServletAspect.ATTRIBUTE_AUTHORIZATION_HANDLE, authorization);
		}
	}
}
