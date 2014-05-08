package de.osthus.ambeth.ioc;

import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

@FrameworkModule
public class EventDataChangeModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.link(EventServerModule.EVENT_MANAGER).to(IEventListenerExtendable.class).with(IDataChange.class);
	}
}
