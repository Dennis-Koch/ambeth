package de.osthus.ambeth.datachange.kafka;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.github.charithe.kafka.KafkaJunitRule;

import de.osthus.ambeth.Ambeth;
import de.osthus.ambeth.bundle.InformationBus;
import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.IRootCache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.datachange.transfer.DataChangeEntry;
import de.osthus.ambeth.datachange.transfer.DataChangeEvent;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.event.IEventListener;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.event.kafka.config.EventKafkaConfigurationConstants;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.CacheModule;
import de.osthus.ambeth.ioc.DataChangeKafkaModule;
import de.osthus.ambeth.ioc.EventKafkaModule;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.XmlModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.kafka.AmbethKafkaConfiguration;
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
import de.osthus.ambeth.testutil.category.PerformanceTests;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.util.ClasspathScanner;
import de.osthus.ambeth.util.IClasspathScanner;

@TestPropertiesList({
// producer
		@TestProperties(name = EventKafkaConfigurationConstants.TOPIC_NAME, value = "test"),//
		@TestProperties(name = AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "acks", value = "all"),//
		@TestProperties(name = AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "retries", value = "0"),//
		@TestProperties(name = AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "batch.size", value = "16384"),//
		@TestProperties(name = AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "linger.ms", value = "1"),//
		@TestProperties(name = AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "buffer.memory", value = "33554432"),//

		// consumer
		@TestProperties(name = AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "group.id", value = "groupId"),//
		@TestProperties(name = AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "enable.auto.commit", value = "true"),//
		@TestProperties(name = AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "auto.commit.interval.ms", value = "1"),//
		@TestProperties(name = AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "session.timeout.ms", value = "30000"),//
		@TestProperties(name = AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "buffer.memory", value = "33554432"),//
		// @TestProperties(name = "ambeth.log.level", value = "DEBUG"),//
})
@Category(PerformanceTests.class)
public class MassDataChangeTest extends AbstractIocTest
{
	private static final int NUM_ENTITIES = 1000;

	public static FutureTask<String> future = new FutureTask<String>(new Callable<String>()

	{
		@Override
		public String call() throws Exception
		{
			return null;
		}
	});

	public static class KafkaTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerBean(ClasspathScanner.class).autowireable(IClasspathScanner.class);
		}
	}

	public static class CounterModule implements IInitializingModule
	{

		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{

			IBeanConfiguration registerExternalBean = beanContextFactory.registerExternalBean(new IEventListener()
			{

				private int globalCounter = 0;

				@Override
				public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) throws Exception
				{
					// wait until all DCE's arrive in the "right" cache.
					if (globalCounter >= NUM_ENTITIES - 1)
					{
						future.run();
					}
					globalCounter++;

				}
			});
			beanContextFactory.link(registerExternalBean).to(IEventListenerExtendable.class).with(DataChangeEvent.class);
		}
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

		props.put(AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "zookeeper.connect", kafkaRule.zookeeperConnectionString());
		// props.put("group.id", "group");
		props.put(AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "bootstrap.servers", "localhost:" + kafkaRule.kafkaBrokerPort());
		props.put(AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "zookeeper.session.timeout.ms", "400");
		props.put(AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "zookeeper.sync.time.ms", "200");
		props.put(AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "auto.commit.interval.ms", "1000");
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
				// Measure processing time with Kafka
				long kafkaTime = testDataChangeEventsWithKafka(app1.getApplicationContext(), app2.getApplicationContext(), NUM_ENTITIES);
				// Measure processing time without Kafka
				long withoutKafkaTime = testContextsWithout(app1.getApplicationContext(), app2.getApplicationContext(), NUM_ENTITIES);
				log.info("Kafka - processing time:" + (kafkaTime - withoutKafkaTime) + "ms " + Math.round(kafkaTime / (withoutKafkaTime / 100) - 100) + " %");
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

	/**
	 * fill up the cache with entities
	 * 
	 * @param left
	 * @param countEntities
	 * @param testEntityList
	 */
	private void fillCache(IServiceContext left, int countEntities, final IList<TestEntity> testEntityList)
	{
		IRootCache leftRootCache;
		{
			TestEntity testEntity;
			for (int i = countEntities; i > 0; i--)
			{

				// create cache entry in "left"
				testEntity = left.getService(IEntityFactory.class).createEntity(TestEntity.class);
				IEntityMetaData metaData = left.getService(IEntityMetaDataProvider.class).getMetaData(TestEntity.class);
				metaData.getIdMember().setIntValue(testEntity, i);
				metaData.getVersionMember().setIntValue(testEntity, 1);

				leftRootCache = left.getService(CacheModule.COMMITTED_ROOT_CACHE, IRootCache.class);
				leftRootCache.put(testEntity);
				testEntityList.add(testEntity);
				Assert.assertNotNull(lookupCacheEntry(leftRootCache, testEntity));

			}
		}
	}

	private long testDataChangeEventsWithKafka(IServiceContext left, final IServiceContext right, int countEntities) throws Throwable
	{
		IRootCache leftRootCache;
		final IList<TestEntity> testEntityList = new ArrayList<TestEntity>();
		right.createService(CounterModule.class);
		fillCache(left, countEntities, testEntityList);
		long mills = System.currentTimeMillis();

		{
			final IBackgroundWorkerDelegate worker = new IBackgroundWorkerDelegate()
			{

				@Override
				public void invoke() throws Throwable
				{
					// fire the DCE in "right"
					for (Iterator iterator = testEntityList.iterator(); iterator.hasNext();)
					{
						TestEntity testEntity = (TestEntity) iterator.next();
						DataChangeEvent dce = DataChangeEvent.create(0, 1, 0);
						dce.getUpdates().add(new DataChangeEntry(TestEntity.class, ObjRef.PRIMARY_KEY_INDEX, testEntity.getId(), testEntity.getVersion() + 1));
						right.getService(IEventDispatcher.class).dispatchEvent(dce);
					}
				}
			};
			new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						worker.invoke();
					}
					catch (Throwable e)
					{
						throw RuntimeExceptionUtil.mask(e);
					}
				}
			}.start();

		}
		log.info("start waiting...");
		// wait for all DCE events to arrive in the right context
		// TODO: is this correct?
		future.get(2, TimeUnit.MINUTES);

		long timeSpend = System.currentTimeMillis() - mills;
		Thread.sleep(2000); // TODO: this should not be needed because the feature "should" only be return if entities have been removed from the cache
		log.info("Kafka DCE for " + NUM_ENTITIES + " DCEs took: " + timeSpend + "ms");
		ensureCacheCleaned(left, testEntityList);
		return timeSpend;
	}

	private void ensureCacheCleaned(IServiceContext left, final IList<TestEntity> testEntityList)
	{
		IRootCache leftRootCache;
		{
			// ensure that entry in "left" is removed
			leftRootCache = left.getService(CacheModule.COMMITTED_ROOT_CACHE, IRootCache.class);

			for (Iterator iterator = testEntityList.iterator(); iterator.hasNext();)
			{
				TestEntity testEntity = (TestEntity) iterator.next();

				Assert.assertNull("Expect entitiy: " + testEntity.getId() + " to be removed from the cache.", lookupCacheEntry(leftRootCache, testEntity));
			}
		}
	}

	private long testContextsWithout(IServiceContext left, final IServiceContext right1, int countEntities) throws Throwable
	{
		IRootCache leftRootCache;
		final IList<TestEntity> testEntityList = new ArrayList<TestEntity>();
		fillCache(left, countEntities, testEntityList);
		long mills = System.currentTimeMillis();
		log.info("start waiting...");
		{
			// fire the DCE in "right"
			for (Iterator iterator = testEntityList.iterator(); iterator.hasNext();)
			{
				TestEntity testEntity = (TestEntity) iterator.next();
				DataChangeEvent dce = DataChangeEvent.create(0, 1, 0);
				dce.getUpdates().add(new DataChangeEntry(TestEntity.class, ObjRef.PRIMARY_KEY_INDEX, testEntity.getId(), testEntity.getVersion() + 1));
				left.getService(IEventDispatcher.class).dispatchEvent(dce);
			}
		}
		long timeSpend = System.currentTimeMillis() - mills;
		Thread.sleep(2000); // TODO: this should not be needed because the feature "should" only be return if entities have been removed from the cache

		log.info("Without Kafka DCE for " + NUM_ENTITIES + " DCEs took: " + timeSpend + "ms");
		ensureCacheCleaned(left, testEntityList);
		return timeSpend;
	}
}
