package com.koch.ambeth.event.kafka;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.event.kafka.config.EventKafkaConfigurationConstants;
import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IdentityWeakHashMap;
import com.koch.ambeth.util.collections.WeakHashMap;
import com.koch.ambeth.util.config.IProperties;

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

	protected final WeakHashMap<Object, Boolean> receivedFromKafkaMap = new IdentityWeakHashMap<Object, Boolean>();

	protected final Lock receivedFromKafkaMapLock = new ReentrantLock();

	protected long timeout = 5000;

	protected volatile boolean destroyed;

	protected KafkaConsumer<String, Object> consumer;

	protected final CountDownLatch terminationLatch = new CountDownLatch(1);

	protected Thread pollingThread;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		pollingThread = new Thread(this);
		pollingThread.setDaemon(true);
		pollingThread.setName(getClass().getSimpleName() + " topic=" + topicName);
	}

	@Override
	public void afterStarted() throws Throwable
	{
		pollingThread.start();
	}

	@Override
	public void destroy() throws Throwable
	{
		destroyed = true;
		pollingThread.interrupt();
		consumer.wakeup();
		if (!terminationLatch.await(30000, TimeUnit.MILLISECONDS))
		{
			log.error(new TimeoutException("Consumer did not exit correctly"));
		}
	}

	public boolean isEventFromKafka(Object evt)
	{
		receivedFromKafkaMapLock.lock();
		try
		{
			return receivedFromKafkaMap.containsKey(evt);
		}
		finally
		{
			receivedFromKafkaMapLock.unlock();
		}
	}

	@Override
	public final void run()
	{
		try
		{
			runIntern();
		}
		catch (Throwable e)
		{
			log.error(e);
		}
		finally
		{
			terminationLatch.countDown();
		}
	}

	protected void runIntern() throws Throwable
	{
		try
		{
			consumer = new KafkaConsumer<String, Object>(AmbethKafkaConfiguration.extractKafkaProperties(props), new StringDeserializer(), xmlKafkaSerializer);
			consumer.subscribe(Arrays.asList(topicName));

			while (!destroyed)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Polling for records on topic '" + topicName + "'...");
				}
				ConsumerRecords<String, Object> records = null;
				try
				{
					records = consumer.poll(timeout);
				}
				catch (WakeupException e)
				{
					// Intended blank
				}
				if (destroyed)
				{
					continue;
				}
				ArrayList<Object> events = new ArrayList<Object>();
				receivedFromKafkaMapLock.lock();
				try
				{
					if (records == null || records.count() == 0)
					{
						// remove the entries of any collected events
						receivedFromKafkaMap.checkForCleanup();
						continue;
					}
					for (ConsumerRecord<String, Object> record : records)
					{
						Object evnt = record.value();
						receivedFromKafkaMap.put(evnt, Boolean.TRUE);
						events.add(evnt);
					}
				}
				finally
				{
					receivedFromKafkaMapLock.unlock();
				}
				eventDispatcher.enableEventQueue();
				try
				{
					for (int a = 0, size = events.size(); a < size; a++)
					{
						eventDispatcher.dispatchEvent(events.get(a));
					}
					if (log.isInfoEnabled())
					{
						log.info("Received " + events.size() + " events from kafka. Flushing queue...");
					}
				}
				finally
				{
					eventDispatcher.flushEventQueue();
				}
			}
		}
		finally
		{
			consumer.close();
		}
	}
}
