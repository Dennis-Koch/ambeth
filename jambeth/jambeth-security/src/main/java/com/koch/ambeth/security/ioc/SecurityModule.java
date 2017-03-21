package com.koch.ambeth.security.ioc;

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

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.security.ILightweightSecurityContext;
import com.koch.ambeth.security.AuthenticatedUserHolder;
import com.koch.ambeth.security.IAuthenticatedUserHolder;
import com.koch.ambeth.security.IAuthorizationChangeListenerExtendable;
import com.koch.ambeth.security.ISecurityContextHolder;
import com.koch.ambeth.security.SecurityContextHolder;

@FrameworkModule
public class SecurityModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(AuthenticatedUserHolder.class).autowireable(IAuthenticatedUserHolder.class);

		beanContextFactory.registerBean(SecurityContextHolder.class).autowireable(ISecurityContextHolder.class, IAuthorizationChangeListenerExtendable.class,
				ILightweightSecurityContext.class);
	}
}
