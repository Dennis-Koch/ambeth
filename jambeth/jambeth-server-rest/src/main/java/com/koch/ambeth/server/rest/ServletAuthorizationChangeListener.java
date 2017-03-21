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

import javax.servlet.http.HttpSession;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.IAuthorizationChangeListener;
import com.koch.ambeth.server.webservice.IHttpSessionProvider;

public class ServletAuthorizationChangeListener implements IAuthorizationChangeListener {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Override
	public void authorizationChanged(IAuthorization authorization) {
		if (authorization == null) {
			return;
		}
		IHttpSessionProvider httpSessionProvider = beanContext.getService(IHttpSessionProvider.class);
		if (httpSessionProvider.getCurrentHttpSession() != null) {
			beanContext.getService(HttpSession.class)
					.setAttribute(AmbethServletRequestFilter.ATTRIBUTE_AUTHORIZATION_HANDLE, authorization);
		}
	}
}
