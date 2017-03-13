package com.koch.ambeth.datachange.kafka.ioc;

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.event.kafka.ioc.EventKafkaModule;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

public class DataChangeKafkaModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.link(EventKafkaModule.EVENT_KAFKA_PUBLISHER).to(IEventListenerExtendable.class).with(IDataChange.class);
	}
}
