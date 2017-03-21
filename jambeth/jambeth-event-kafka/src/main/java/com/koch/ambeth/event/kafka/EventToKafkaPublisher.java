package com.koch.ambeth.event.kafka;

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

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import com.koch.ambeth.event.IBatchedEventListener;
import com.koch.ambeth.event.IEventListener;
import com.koch.ambeth.event.IEventQueue;
import com.koch.ambeth.event.kafka.config.EventKafkaConfigurationConstants;
import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.config.IProperties;

public class EventToKafkaPublisher implements IEventListener, IBatchedEventListener, IInitializingBean, IDisposableBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEventQueue eventQueue;

	@Autowired
	protected EventFromKafkaConsumer eventFromKafkaConsumer;

	@Autowired
	protected XmlKafkaSerializer xmlKafkaSerializer;

	@Autowired
	protected IProperties props;

	@Property(name = EventKafkaConfigurationConstants.TOPIC_NAME)
	protected String topicName;

	private Producer<String, Object> producer;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		producer = new KafkaProducer<String, Object>(AmbethKafkaConfiguration.extractKafkaProperties(props), new StringSerializer(), xmlKafkaSerializer);
	}

	@Override
	public void destroy() throws Throwable
	{
		producer.close();
	}

	@Override
	public void enableBatchedEventDispatching()
	{
		// intended blank
	}

	@Override
	public void flushBatchedEventDispatching()
	{
		producer.flush();
	}

	@Override
	public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) throws Exception
	{
		if (eventFromKafkaConsumer.isEventFromKafka(eventObject))
		{
			return;
		}
		// if (log.isDebugEnabled())
		// {
		// log.debug("Publish event of type '" + eventObject.getClass() + "' to kafka...");
		// }
		producer.send(new ProducerRecord<String, Object>(topicName, null, eventObject));
		if (!eventQueue.isDispatchingBatchedEvents())
		{
			producer.flush();
		}
	}
}
