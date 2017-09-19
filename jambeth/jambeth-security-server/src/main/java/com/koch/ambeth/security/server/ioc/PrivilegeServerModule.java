package com.koch.ambeth.security.server.ioc;

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

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.security.server.privilege.IEntityPermissionRuleExtendable;
import com.koch.ambeth.security.server.privilege.IEntityPermissionRuleProvider;
import com.koch.ambeth.security.server.privilege.IEntityTypePermissionRuleExtendable;
import com.koch.ambeth.security.server.privilege.IEntityTypePermissionRuleProvider;
import com.koch.ambeth.security.server.service.PrivilegeService;
import com.koch.ambeth.security.service.IPrivilegeService;

@FrameworkModule
public class PrivilegeServerModule implements IInitializingModule {
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		beanContextFactory.registerBean(PrivilegeService.class).autowireable(//
				IPrivilegeService.class, //
				IEntityPermissionRuleExtendable.class, IEntityTypePermissionRuleExtendable.class, //
				IEntityPermissionRuleProvider.class, IEntityTypePermissionRuleProvider.class);

	}
}
