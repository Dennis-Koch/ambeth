package com.koch.ambeth.event.datachange.ioc;

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.event.config.EventConfigurationConstants;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

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
