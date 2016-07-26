package de.osthus.ambeth.event.kafka;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.event.IBatchedEventListener;
import de.osthus.ambeth.event.IEventListener;
import de.osthus.ambeth.event.IEventQueue;
import de.osthus.ambeth.event.kafka.config.EventKafkaConfigurationConstants;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class EventToKafkaPublisher implements IEventListener, IBatchedEventListener, IInitializingBean, IDisposableBean
{
	public static final String AMBETH_KAFKA_PROP_PREFIX = "ambeth.kafka.";

	public static Properties extractKafkaProperties(IProperties props)
	{
		Properties kafkaProps = new Properties();
		for (String key : props.collectAllPropertyKeys())
		{
			if (!key.startsWith(AMBETH_KAFKA_PROP_PREFIX))
			{
				continue;
			}
			String kafkaKey = key.substring(AMBETH_KAFKA_PROP_PREFIX.length());
			kafkaProps.put(kafkaKey, props.get(key));
		}
		return kafkaProps;
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEventQueue eventQueue;

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
		producer = new KafkaProducer<String, Object>(extractKafkaProperties(props), new StringSerializer(), xmlKafkaSerializer);
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
		if (log.isDebugEnabled())
		{
			log.debug("Publish event of type '" + eventObject.getClass() + "' to kafka...");
		}
		producer.send(new ProducerRecord<String, Object>(topicName, null, eventObject));
		if (!eventQueue.isDispatchingBatchedEvents())
		{
			producer.flush();
		}
	}
}
