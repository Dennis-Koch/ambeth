package de.osthus.ambeth.event.kafka;

import java.util.Arrays;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.event.kafka.config.EventKafkaConfigurationConstants;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class EventFromKafkaConsumer implements IInitializingBean, IStartingBean, IDisposableBean, Runnable
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEventDispatcher eventDispatcher;

	@Autowired
	protected IProperties props;

	@Autowired
	protected XmlKafkaSerializer xmlKafkaSerializer;

	@Property(name = EventKafkaConfigurationConstants.TOPIC_NAME)
	protected String topicName;

	protected long timeout = 5000;

	protected volatile boolean destroyed;

	protected KafkaConsumer<String, Object> consumer;

	protected Thread pollingThread;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		pollingThread = new Thread(this);
		pollingThread.setDaemon(true);
		pollingThread.setName(getClass().getSimpleName() + "-" + topicName);

		consumer = new KafkaConsumer<String, Object>(EventToKafkaPublisher.extractKafkaProperties(props), new StringDeserializer(), xmlKafkaSerializer);
	}

	@Override
	public void afterStarted() throws Throwable
	{
		consumer.subscribe(Arrays.asList(topicName));
		pollingThread.start();
	}

	@Override
	public void destroy() throws Throwable
	{
		destroyed = true;
	}

	@Override
	public void run()
	{
		try
		{
			while (!destroyed)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Polling for records...");
				}
				ConsumerRecords<String, Object> records = consumer.poll(timeout);
				if (destroyed || records.count() == 0)
				{
					continue;
				}
				eventDispatcher.enableEventQueue();
				try
				{
					for (ConsumerRecord<String, Object> record : records)
					{
						Object value = record.value();
						if (log.isDebugEnabled())
						{
							log.debug("Received record with key '" + record.key() + "'");
						}
						eventDispatcher.dispatchEvent(value);
					}
				}
				finally
				{
					eventDispatcher.flushEventQueue();
				}
			}
		}
		catch (Throwable e)
		{
			log.error(e);
		}
		finally
		{
			consumer.close();
		}
	}
}
