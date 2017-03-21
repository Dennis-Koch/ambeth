package com.koch.ambeth.event.kafka.ioc;

/*-
 * #%L
 * jambeth-event-kafka
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.event.kafka.EventFromKafkaConsumer;
import com.koch.ambeth.event.kafka.EventToKafkaPublisher;
import com.koch.ambeth.event.kafka.XmlKafkaSerializer;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

public class EventKafkaModule implements IInitializingModule {
	public static final String EVENT_KAFKA_PUBLISHER = "eventKafkaPublisher";

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		IBeanConfiguration eventFromKafkaConsumer =
				beanContextFactory.registerBean(EventFromKafkaConsumer.class);

		beanContextFactory.registerBean(EVENT_KAFKA_PUBLISHER, EventToKafkaPublisher.class)
				.propertyRef(eventFromKafkaConsumer);

		beanContextFactory.registerBean(XmlKafkaSerializer.class)
				.autowireable(XmlKafkaSerializer.class);
	}
}
