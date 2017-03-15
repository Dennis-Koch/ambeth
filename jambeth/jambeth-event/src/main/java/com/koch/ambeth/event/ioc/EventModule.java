package com.koch.ambeth.event.ioc;

import com.koch.ambeth.event.EventListenerRegistry;
import com.koch.ambeth.event.EventPoller;
import com.koch.ambeth.event.IEventBatcher;
import com.koch.ambeth.event.IEventBatcherExtendable;
import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.event.IEventListener;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.event.IEventQueue;
import com.koch.ambeth.event.IEventTargetExtractorExtendable;
import com.koch.ambeth.event.IEventTargetListenerExtendable;
import com.koch.ambeth.event.config.EventConfigurationConstants;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.IOfflineListenerExtendable;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;

@FrameworkModule
public class EventModule implements IInitializingModule {
	@LogInstance
	private ILogger log;

	@Property(name = ServiceConfigurationConstants.NetworkClientMode, defaultValue = "false")
	protected boolean isNetworkClientMode;

	@Property(name = EventConfigurationConstants.PollingActive, defaultValue = "false")
	protected boolean isPollingActive;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		beanContextFactory.registerBean(EventListenerRegistry.class).autowireable(
				IEventListenerExtendable.class, IEventTargetListenerExtendable.class,
				IEventBatcherExtendable.class, IEventTargetExtractorExtendable.class, IEventBatcher.class,
				IEventDispatcher.class, IEventListener.class, IEventQueue.class);

		if (isNetworkClientMode) {
			if (isPollingActive) {
				beanContextFactory.registerBean("eventPoller", EventPoller.class);
				beanContextFactory.link("eventPoller").to(IOfflineListenerExtendable.class);
			}
			else {
				if (log.isInfoEnabled()) {
					log.info("Event polling disabled. Reason: property '"
							+ EventConfigurationConstants.PollingActive + "' set to '" + isPollingActive + "'");
				}
			}
		}
	}
}
