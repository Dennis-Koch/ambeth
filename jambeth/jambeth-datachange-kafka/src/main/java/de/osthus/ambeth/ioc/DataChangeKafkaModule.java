package de.osthus.ambeth.ioc;

import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public class DataChangeKafkaModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.link(EventKafkaModule.EVENT_KAFKA_PUBLISHER).to(IEventListenerExtendable.class).with(IDataChange.class);
	}
}
