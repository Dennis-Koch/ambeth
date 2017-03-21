package com.koch.ambeth.security.server;

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

import java.lang.reflect.Method;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.security.CallPermission;
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.IServiceFilter;
import com.koch.ambeth.security.SecurityContextType;
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class DefaultServiceFilter implements IServiceFilter
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public CallPermission checkCallPermissionOnService(Method method, Object[] arguments, SecurityContextType securityContextType,
			IAuthorization authorization, ISecurityScope[] securityScopes)
	{
		if (authorization == null || !authorization.isValid())
		{
			if (SecurityContextType.NOT_REQUIRED.equals(securityContextType))
			{
				return CallPermission.ALLOWED;
			}
			return CallPermission.FORBIDDEN;
		}
		return authorization.getCallPermission(method, securityScopes);
	}
}
