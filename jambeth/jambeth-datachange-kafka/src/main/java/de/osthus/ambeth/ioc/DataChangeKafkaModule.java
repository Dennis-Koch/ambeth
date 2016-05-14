package de.osthus.ambeth.ioc;

import de.osthus.ambeth.datachange.kafka.DataChangeKafkaBridge;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public class DataChangeKafkaModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		IBeanConfiguration kafkaDataChangeEventListener = beanContextFactory.registerBean(DataChangeKafkaBridge.class).propertyRefs(
				EventKafkaModule.EVENT_KAFKA_PUBLISHER);
		beanContextFactory.link(kafkaDataChangeEventListener).to(IEventListenerExtendable.class).with(IDataChange.class);
	}
}
