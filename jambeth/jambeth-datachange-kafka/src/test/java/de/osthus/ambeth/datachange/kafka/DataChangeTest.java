package de.osthus.ambeth.datachange.kafka;

import java.util.EnumSet;

import javax.xml.bind.annotation.XmlRootElement;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.github.charithe.kafka.KafkaJunitRule;

import de.osthus.ambeth.Ambeth;
import de.osthus.ambeth.bundle.InformationBus;
import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.IRootCache;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.datachange.transfer.DataChangeEntry;
import de.osthus.ambeth.datachange.transfer.DataChangeEvent;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.event.kafka.EventToKafkaPublisher;
import de.osthus.ambeth.event.kafka.config.EventKafkaConfigurationConstants;
import de.osthus.ambeth.ioc.CacheModule;
import de.osthus.ambeth.ioc.DataChangeKafkaModule;
import de.osthus.ambeth.ioc.EventKafkaModule;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.XmlModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.start.IAmbethApplication;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.util.ClasspathScanner;
import de.osthus.ambeth.util.IClasspathScanner;

@TestPropertiesList({
// producer
		@TestProperties(name = EventKafkaConfigurationConstants.TOPIC_NAME, value = "test"),//
		@TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "acks", value = "all"),//
		@TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "retries", value = "0"),//
		@TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "batch.size", value = "16384"),//
		@TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "linger.ms", value = "1"),//
		@TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "buffer.memory", value = "33554432"),//

		// consumer
		@TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "group.id", value = "groupId"),//
		@TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "enable.auto.commit", value = "true"),//
		@TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "auto.commit.interval.ms", value = "1"),//
		@TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "session.timeout.ms", value = "30000"),//
		@TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "buffer.memory", value = "33554432"),//
})
public class DataChangeTest extends AbstractIocTest
{
	public static class KafkaTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerBean(ClasspathScanner.class).autowireable(IClasspathScanner.class);
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
		props.put(ServiceConfigurationConstants.mappingFile, "orm.xml");

		IAmbethApplication app1 = Ambeth.createBundle(InformationBus.class)
				.withAmbethModules(EventKafkaModule.class, DataChangeKafkaModule.class, KafkaTestModule.class, XmlModule.class).withoutPropertiesFileSearch()
				.withProperties(props).start();
		try
		{
			IAmbethApplication app2 = Ambeth.createBundle(InformationBus.class)
					.withAmbethModules(EventKafkaModule.class, DataChangeKafkaModule.class, KafkaTestModule.class, XmlModule.class)
					.withoutPropertiesFileSearch().withProperties(props).start();
			try
			{
				testContexts(app1.getApplicationContext(), app2.getApplicationContext());
			}
			finally
			{
				app2.close();
			}
		}
		finally
		{
			app1.close();
		}
	}

	protected Object lookupCacheEntry(IRootCache rootCache, TestEntity testEntity)
	{
		return rootCache.getObject(new ObjRef(TestEntity.class, ObjRef.PRIMARY_KEY_INDEX, testEntity.getId(), null),
				EnumSet.of(CacheDirective.FailEarly, CacheDirective.LoadContainerResult));
	}

	private void testContexts(IServiceContext left, IServiceContext right) throws Throwable
	{
		IRootCache leftRootCache;
		TestEntity testEntity;
		{
			IEntityFactory entityFactory = left.getService(IEntityFactory.class);
			IEntityMetaDataProvider entityMetaDataProvider = left.getService(IEntityMetaDataProvider.class);
			leftRootCache = left.getService(CacheModule.COMMITTED_ROOT_CACHE, IRootCache.class);

			// create cache entry in "left"
			testEntity = entityFactory.createEntity(TestEntity.class);
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(TestEntity.class);
			metaData.getIdMember().setIntValue(testEntity, 1);
			metaData.getVersionMember().setIntValue(testEntity, 1);

			leftRootCache.put(testEntity);

			Assert.assertNotNull(lookupCacheEntry(leftRootCache, testEntity));
		}
		{
			IEventDispatcher eventDispatcher = right.getService(IEventDispatcher.class);

			// fire the DCE in "right"
			DataChangeEvent dce = DataChangeEvent.create(0, 1, 0);
			dce.getUpdates().add(new DataChangeEntry(TestEntity.class, ObjRef.PRIMARY_KEY_INDEX, testEntity.getId(), testEntity.getVersion() + 1));
			eventDispatcher.dispatchEvent(dce);
		}
		Thread.sleep(2000);
		{
			// ensure that entry in "left" is removed
			Assert.assertNull(lookupCacheEntry(leftRootCache, testEntity));
		}
	}
}
