package de.osthus.ambeth.ioc;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.event.EventListenerRegistry;
import de.osthus.ambeth.event.EventPoller;
import de.osthus.ambeth.event.IEventBatcher;
import de.osthus.ambeth.event.IEventBatcherExtendable;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.event.IEventListener;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.event.IEventQueue;
import de.osthus.ambeth.event.IEventTargetExtractorExtendable;
import de.osthus.ambeth.event.config.EventConfigurationConstants;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.service.IOfflineListenerExtendable;
import de.osthus.ambeth.service.config.ConfigurationConstants;

@FrameworkModule
public class EventModule implements IInitializingModule
{
	@LogInstance(EventModule.class)
	private ILogger log;

	protected boolean isNetworkClientMode;

	protected boolean isPollingActive;

	@Property(name = ConfigurationConstants.NetworkClientMode, defaultValue = "false")
	public void setNetworkClientMode(boolean isNetworkClientMode)
	{
		this.isNetworkClientMode = isNetworkClientMode;
	}

	@Property(name = EventConfigurationConstants.PollingActive, defaultValue = "false")
	public void setPollingActive(boolean isPollingActive)
	{
		this.isPollingActive = isPollingActive;
	}

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAnonymousBean(EventListenerRegistry.class).autowireable(IEventListenerExtendable.class, IEventBatcherExtendable.class,
				IEventTargetExtractorExtendable.class, IEventBatcher.class, IEventDispatcher.class, IEventListener.class, IEventQueue.class);

		if (isNetworkClientMode)
		{
			if (isPollingActive)
			{
				beanContextFactory.registerBean("eventPoller", EventPoller.class);
				beanContextFactory.link("eventPoller").to(IOfflineListenerExtendable.class);
			}
			else
			{
				if (log.isInfoEnabled())
				{
					log.info("Event polling disabled. Reason: property '" + EventConfigurationConstants.PollingActive + "' set to '" + isPollingActive + "'");
				}
			}
		}
	}
}
