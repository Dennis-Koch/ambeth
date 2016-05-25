package de.osthus.ambeth.event.kafka;

import javax.xml.bind.annotation.XmlRootElement;

import org.junit.Rule;
import org.junit.Test;

import com.github.charithe.kafka.KafkaJunitRule;

import de.osthus.ambeth.Ambeth;
import de.osthus.ambeth.bundle.InformationBus;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.datachange.transfer.DataChangeEntry;
import de.osthus.ambeth.datachange.transfer.DataChangeEvent;
import de.osthus.ambeth.event.IEventListener;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.event.kafka.config.EventKafkaConfigurationConstants;
import de.osthus.ambeth.ioc.EventKafkaModule;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.XmlModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.start.IAmbethApplication;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.util.ClasspathScanner;
import de.osthus.ambeth.util.IClasspathScanner;

@TestPropertiesList({
// @TestProperties(name = KafkaDataChangePublisher.AMBETH_KAFKA_PROP_PREFIX + "bootstrap.servers", value = "localhost:4242"),//

		// producer
		@TestProperties(name = EventKafkaConfigurationConstants.DCE_TOPIC_NAME, value = "test"),//
		@TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "acks", value = "all"),//
		@TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "retries", value = "0"),//
		@TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "batch.size", value = "16384"),//
		@TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "linger.ms", value = "1"),//
		@TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "buffer.memory", value = "33554432"),//

		// consumer
		@TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "group.id", value = "groupId"),//
		// @TestProperties(name = KafkaDataChangePublisher.AMBETH_KAFKA_PROP_PREFIX + "partition.assignment.strategy", value = "groupId"),//
		@TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "enable.auto.commit", value = "true"),//
		@TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "auto.commit.interval.ms", value = "1"),//
		@TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "session.timeout.ms", value = "30000"),//
		@TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "buffer.memory", value = "33554432"),//
})
public class KafkaTest extends AbstractIocTest
{
	public static class KafkaTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerBean(ClasspathScanner.class).autowireable(IClasspathScanner.class);
			beanContextFactory.link(EventKafkaModule.EVENT_KAFKA_PUBLISHER).to(IEventListenerExtendable.class).with(MyMessage.class);
		}
	}

	@XmlRootElement
	public static class MyMessage
	{

	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Rule
	public KafkaJunitRule kafkaRule = new KafkaJunitRule();

	@Autowired
	protected IProperties properties;

	@Test
	public void test() throws Throwable
	{
		Properties props = new Properties(properties);

		props.put(EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "zookeeper.connect", kafkaRule.zookeeperConnectionString());
		// props.put("group.id", "group");
		props.put(EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "bootstrap.servers", "localhost:" + kafkaRule.kafkaBrokerPort());
		props.put(EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "zookeeper.session.timeout.ms", "400");
		props.put(EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "zookeeper.sync.time.ms", "200");
		props.put(EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "auto.commit.interval.ms", "1000");

		// props.put("bootstrap.servers", );

		IAmbethApplication app = Ambeth.createBundle(InformationBus.class).withAmbethModules(EventKafkaModule.class, KafkaTestModule.class, XmlModule.class)
				.withoutPropertiesFileSearch().withProperties(props).start();
		try
		{
			IEventListener eventListener = app.getApplicationContext().getService(IEventListener.class);
			DataChangeEvent dce = DataChangeEvent.create(1, 0, 0);
			dce.getInserts().add(new DataChangeEntry());
			eventListener.handleEvent(dce, -1, -1);
		}
		finally
		{
			app.close();
		}
	}
}
