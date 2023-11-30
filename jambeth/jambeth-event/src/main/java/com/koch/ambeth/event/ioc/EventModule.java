package com.koch.ambeth.event.ioc;

/*-
 * #%L
 * jambeth-event
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
import com.koch.ambeth.event.EventListenerRegistry;
import com.koch.ambeth.event.EventPoller;
import com.koch.ambeth.event.IEventBatcher;
import com.koch.ambeth.event.IEventBatcherExtendable;
import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.event.IEventListener;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.event.IEventPoller;
import com.koch.ambeth.event.IEventQueue;
import com.koch.ambeth.event.IEventTargetExtractorExtendable;
import com.koch.ambeth.event.IEventTargetListenerExtendable;
import com.koch.ambeth.event.config.EventConfigurationConstants;
import com.koch.ambeth.event.service.IEventService;
import com.koch.ambeth.ioc.IFrameworkModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.IOfflineListenerExtendable;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.remote.ClientServiceBean;
import com.koch.ambeth.util.event.ILightweightEventQueue;

@SpiService(IFrameworkModule.class)
@FrameworkModule
public class EventModule implements IFrameworkModule {
    @Property(name = ServiceConfigurationConstants.NetworkClientMode, defaultValue = "false")
    protected boolean isNetworkClientMode;
    @Property(name = EventConfigurationConstants.PollingActive, defaultValue = "true")
    protected boolean isPollingActive;
    @Property(name = EventConfigurationConstants.EventServiceBeanActive, defaultValue = "true")
    protected boolean isEventServiceBeanActive;
    @LogInstance
    private ILogger log;

    @Override
    public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
        beanContextFactory.registerBean(EventListenerRegistry.class)
                          .autowireable(IEventListenerExtendable.class, IEventTargetListenerExtendable.class, IEventBatcherExtendable.class, IEventTargetExtractorExtendable.class, IEventBatcher.class,
                                  IEventDispatcher.class, IEventListener.class, IEventQueue.class, ILightweightEventQueue.class);

        if (isNetworkClientMode && isEventServiceBeanActive) {
            beanContextFactory.registerBean("eventService.external", ClientServiceBean.class)
                              .propertyValue(ClientServiceBean.INTERFACE_PROP_NAME, IEventService.class)
                              .autowireable(IEventService.class);

            if (isPollingActive) {
                var eventPoller = beanContextFactory.registerBean(EventPoller.class).autowireable(IEventPoller.class);
                beanContextFactory.link(eventPoller).to(IOfflineListenerExtendable.class);
            } else {
                if (log.isInfoEnabled()) {
                    log.info("Event polling disabled. Reason: property '" + EventConfigurationConstants.PollingActive + "' set to '" + isPollingActive + "'");
                }
            }
        }
    }
}
