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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;

public class AuthenticatedUserHolder implements IAuthenticatedUserHolder, IThreadLocalCleanupBean {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Forkable
	protected final ThreadLocal<String> authenticatedUserTL = new SensitiveThreadLocal<>();

	@Override
	public void cleanupThreadLocal() {
		if (authenticatedUserTL.get() != null) {
			throw new IllegalStateException(
					"At this point the thread-local connection has to be already cleaned up gracefully");
		}
	}

	@Override
	public String getAuthenticatedSID() {
		String authorizedUser = authenticatedUserTL.get();
		if (authorizedUser != null) {
			return authorizedUser;
		}
		ISecurityContext context = securityContextHolder.getContext();
		IAuthorization authorization = context != null ? context.getAuthorization() : null;
		return authorization != null ? authorization.getSID() : null;
	}

	@Override
	public void setAuthenticatedSID(String sid) {
		authenticatedUserTL.set(sid);
	}
}
