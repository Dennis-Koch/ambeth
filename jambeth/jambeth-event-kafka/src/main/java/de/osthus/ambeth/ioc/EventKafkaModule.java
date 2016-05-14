package de.osthus.ambeth.ioc;

import de.osthus.ambeth.event.kafka.EventFromKafkaConsumer;
import de.osthus.ambeth.event.kafka.EventToKafkaPublisher;
import de.osthus.ambeth.event.kafka.XmlKafkaSerializer;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public class EventKafkaModule implements IInitializingModule
{
	public static final String EVENT_KAFKA_PUBLISHER = "eventKafkaPublisher";

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(EventFromKafkaConsumer.class);

		beanContextFactory.registerBean(EVENT_KAFKA_PUBLISHER, EventToKafkaPublisher.class);

		beanContextFactory.registerBean(XmlKafkaSerializer.class).autowireable(XmlKafkaSerializer.class);
	}
}
