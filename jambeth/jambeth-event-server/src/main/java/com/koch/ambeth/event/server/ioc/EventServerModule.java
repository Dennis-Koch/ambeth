package com.koch.ambeth.event.server.ioc;

import com.koch.ambeth.event.config.EventConfigurationConstants;
import com.koch.ambeth.event.server.EventManager;
import com.koch.ambeth.event.server.IEventProvider;
import com.koch.ambeth.event.server.IEventStore;
import com.koch.ambeth.event.server.services.EventService;
import com.koch.ambeth.event.service.IEventService;
import com.koch.ambeth.event.store.IEventStoreHandlerExtendable;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IPropertyLoadingBean;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.config.Properties;

@FrameworkModule
public class EventServerModule implements IInitializingModule, IPropertyLoadingBean
{
	public static final String EVENT_MANAGER = "eventManager";

	@Override
	public void applyProperties(Properties contextProperties)
	{
		contextProperties.putString(EventConfigurationConstants.EventManagerName, EVENT_MANAGER);
	}

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(EVENT_MANAGER, EventManager.class).autowireable(IEventProvider.class, IEventStore.class,
				IEventStoreHandlerExtendable.class);

		beanContextFactory.registerBean("eventService", EventService.class).autowireable(IEventService.class);
	}
}
