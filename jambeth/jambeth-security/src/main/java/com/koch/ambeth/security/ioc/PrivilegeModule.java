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

import com.koch.ambeth.datachange.UnfilteredDataChangeListener;
import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.security.privilege.IPrivilegeProvider;
import com.koch.ambeth.security.privilege.IPrivilegeProviderIntern;
import com.koch.ambeth.security.privilege.PrivilegeProvider;
import com.koch.ambeth.security.privilege.factory.EntityPrivilegeFactoryProvider;
import com.koch.ambeth.security.privilege.factory.EntityTypePrivilegeFactoryProvider;
import com.koch.ambeth.security.privilege.factory.IEntityPrivilegeFactoryProvider;
import com.koch.ambeth.security.privilege.factory.IEntityTypePrivilegeFactoryProvider;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;

@FrameworkModule
public class PrivilegeModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		IBeanConfiguration privilegeProvider = beanContextFactory.registerBean(PrivilegeProvider.class).autowireable(IPrivilegeProvider.class,
				IPrivilegeProviderIntern.class);
		IBeanConfiguration ppEventListener = beanContextFactory.registerBean(UnfilteredDataChangeListener.class).propertyRefs(privilegeProvider);
		beanContextFactory.link(ppEventListener).to(IEventListenerExtendable.class).with(IDataChange.class);
		beanContextFactory.link(privilegeProvider, "handleClearAllCaches").to(IEventListenerExtendable.class).with(ClearAllCachesEvent.class);

		beanContextFactory.registerBean(EntityPrivilegeFactoryProvider.class).autowireable(IEntityPrivilegeFactoryProvider.class);
		beanContextFactory.registerBean(EntityTypePrivilegeFactoryProvider.class).autowireable(IEntityTypePrivilegeFactoryProvider.class);

		// if (IsNetworkClientMode && IsPrivilegeServiceBeanActive)
		// {
		// beanContextFactory.registerBean<ClientServiceBean>("privilegeServiceWCF")
		// .propertyValue("Interface", typeof(IPrivilegeService))
		// .propertyValue("SyncRemoteInterface", typeof(IPrivilegeServiceWCF))
		// .propertyValue("AsyncRemoteInterface", typeof(IPrivilegeClient)).autowireable<IPrivilegeService>();
		// }
	}
}
