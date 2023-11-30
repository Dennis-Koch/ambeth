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

import io.toolisticon.spiap.api.SpiService;
import com.koch.ambeth.datachange.UnfilteredDataChangeListener;
import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.ioc.IFrameworkModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.security.config.SecurityConfigurationConstants;
import com.koch.ambeth.security.events.ClearAllCachedPrivilegesEvent;
import com.koch.ambeth.security.privilege.IPrivilegeProvider;
import com.koch.ambeth.security.privilege.IPrivilegeProviderIntern;
import com.koch.ambeth.security.privilege.PrivilegeProvider;
import com.koch.ambeth.security.privilege.factory.EntityPrivilegeFactoryProvider;
import com.koch.ambeth.security.privilege.factory.EntityTypePrivilegeFactoryProvider;
import com.koch.ambeth.security.privilege.factory.IEntityPrivilegeFactoryProvider;
import com.koch.ambeth.security.privilege.factory.IEntityTypePrivilegeFactoryProvider;
import com.koch.ambeth.security.service.IPrivilegeService;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.remote.ClientServiceBean;

@SpiService(IFrameworkModule.class)
@FrameworkModule
public class PrivilegeModule implements IFrameworkModule {
    public static final String PRIVILEGE_PROVIDER_BEAN_NAME = "privilegeProvider";

    @Property(name = ServiceConfigurationConstants.NetworkClientMode, defaultValue = "false")
    protected boolean isNetworkClientMode;

    @Property(name = SecurityConfigurationConstants.PrivilegeServiceBeanActive, defaultValue = "true")
    protected boolean isPrivilegeServiceBeanActive;

    @Override
    public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
        IBeanConfiguration privilegeProvider =
                beanContextFactory.registerBean(PRIVILEGE_PROVIDER_BEAN_NAME, PrivilegeProvider.class).autowireable(IPrivilegeProvider.class, IPrivilegeProviderIntern.class);
        IBeanConfiguration ppEventListener = beanContextFactory.registerBean(UnfilteredDataChangeListener.class).propertyRefs(privilegeProvider);
        beanContextFactory.link(ppEventListener).to(IEventListenerExtendable.class).with(IDataChange.class);
        beanContextFactory.link(privilegeProvider, PrivilegeProvider.HANDLE_CLEAR_ALL_CACHES).to(IEventListenerExtendable.class).with(ClearAllCachesEvent.class);
        beanContextFactory.link(privilegeProvider, PrivilegeProvider.HANDLE_CLEAR_ALL_PRIVILEGES).to(IEventListenerExtendable.class).with(ClearAllCachedPrivilegesEvent.class);

        beanContextFactory.registerBean(EntityPrivilegeFactoryProvider.class).autowireable(IEntityPrivilegeFactoryProvider.class);
        beanContextFactory.registerBean(EntityTypePrivilegeFactoryProvider.class).autowireable(IEntityTypePrivilegeFactoryProvider.class);

        if (isNetworkClientMode && isPrivilegeServiceBeanActive) {
            beanContextFactory.registerBean("privilegeService.external", ClientServiceBean.class)
                              .propertyValue(ClientServiceBean.INTERFACE_PROP_NAME, IPrivilegeService.class)
                              .autowireable(IPrivilegeService.class);
        }
    }
}
