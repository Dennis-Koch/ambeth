package de.osthus.ambeth.ioc;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.event.EventManager;
import de.osthus.ambeth.event.IEventProvider;
import de.osthus.ambeth.event.IEventStore;
import de.osthus.ambeth.event.config.EventConfigurationConstants;
import de.osthus.ambeth.event.store.IEventStoreHandlerExtendable;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.service.IEventService;
import de.osthus.ambeth.services.EventService;

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
