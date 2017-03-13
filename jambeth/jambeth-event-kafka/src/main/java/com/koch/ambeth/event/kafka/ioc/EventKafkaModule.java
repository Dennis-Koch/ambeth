package com.koch.ambeth.event.kafka.ioc;

import com.koch.ambeth.event.kafka.EventFromKafkaConsumer;
import com.koch.ambeth.event.kafka.EventToKafkaPublisher;
import com.koch.ambeth.event.kafka.XmlKafkaSerializer;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

public class EventKafkaModule implements IInitializingModule
{
	public static final String EVENT_KAFKA_PUBLISHER = "eventKafkaPublisher";

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		IBeanConfiguration eventFromKafkaConsumer = beanContextFactory.registerBean(EventFromKafkaConsumer.class);

		beanContextFactory.registerBean(EVENT_KAFKA_PUBLISHER, EventToKafkaPublisher.class).propertyRef(eventFromKafkaConsumer);

		beanContextFactory.registerBean(XmlKafkaSerializer.class).autowireable(XmlKafkaSerializer.class);
	}
}
