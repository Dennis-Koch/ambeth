package com.koch.ambeth.event.server.ioc;

/*-
 * #%L
 * jambeth-event-server
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
import com.koch.ambeth.event.config.EventConfigurationConstants;
import com.koch.ambeth.event.server.EventManager;
import com.koch.ambeth.event.server.IEventProvider;
import com.koch.ambeth.event.server.IEventStore;
import com.koch.ambeth.event.server.services.EventService;
import com.koch.ambeth.event.service.IEventService;
import com.koch.ambeth.event.store.IEventStoreHandlerExtendable;
import com.koch.ambeth.ioc.IFrameworkModule;
import com.koch.ambeth.ioc.IPropertyLoadingBean;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.config.Properties;

@SpiService(IFrameworkModule.class)
@FrameworkModule
public class EventServerModule implements IFrameworkModule, IPropertyLoadingBean {
    public static final String EVENT_MANAGER = "eventManager";

    @Override
    public void applyProperties(Properties contextProperties) {
        contextProperties.putString(EventConfigurationConstants.EventManagerName, EVENT_MANAGER);
    }

    @Override
    public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
        beanContextFactory.registerBean(EVENT_MANAGER, EventManager.class).autowireable(IEventProvider.class, IEventStore.class, IEventStoreHandlerExtendable.class);

        beanContextFactory.registerBean("eventService", EventService.class).autowireable(IEventService.class);
    }
}
