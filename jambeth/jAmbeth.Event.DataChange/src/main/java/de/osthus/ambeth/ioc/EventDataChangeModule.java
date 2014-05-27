package de.osthus.ambeth.ioc;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.event.config.EventConfigurationConstants;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

@FrameworkModule
public class EventDataChangeModule implements IInitializingModule
{
	@Property(name = EventConfigurationConstants.EventManagerName, mandatory = false)
	protected String eventManagerName;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		if (eventManagerName != null)
		{
			beanContextFactory.link(eventManagerName).to(IEventListenerExtendable.class).with(IDataChange.class);
		}
	}
}
