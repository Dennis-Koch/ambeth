package com.koch.ambeth.datachange.kafka;

/*-
 * #%L
 * jambeth-datachange-kafka-test
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

import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.cache.ioc.CacheModule;
import com.koch.ambeth.core.Ambeth;
import com.koch.ambeth.core.start.IAmbethApplication;
import com.koch.ambeth.datachange.kafka.ioc.DataChangeKafkaModule;
import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.datachange.transfer.DataChangeEntry;
import com.koch.ambeth.datachange.transfer.DataChangeEvent;
import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.event.IEventListener;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.event.kafka.AmbethKafkaConfiguration;
import com.koch.ambeth.event.kafka.AmbethKafkaJunitRule;
import com.koch.ambeth.event.kafka.config.EventKafkaConfigurationConstants;
import com.koch.ambeth.event.kafka.ioc.EventKafkaModule;
import com.koch.ambeth.informationbus.InformationBus;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.log.LoggerFactory;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.testutil.category.SlowTests;
import com.koch.ambeth.util.IClasspathScanner;
import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.xml.ioc.XmlModule;
import com.koch.ambeth.xml.util.ClasspathScanner;

@Category(SlowTests.class)
@TestPropertiesList({
		// producer
		@TestProperties(name = EventKafkaConfigurationConstants.TOPIC_NAME, value = "test"), //
		// @TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "acks", value =
		// "all"),//
		// @TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "retries", value =
		// "0"),//
		// @TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "batch.size", value =
		// "16384"),//
		// @TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "linger.ms", value =
		// "1"),//
		// @TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "buffer.memory",
		// value = "33554432"),//
		//
		// consumer
		@TestProperties(name = AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX
				+ AmbethKafkaConfiguration.CONS_GROUP_ID, value = "groupId"), //
		// @TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "enable.auto.commit",
		// value = "true"),//
		// @TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX +
		// "auto.commit.interval.ms", value = "1"),//
		// @TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "session.timeout.ms",
		// value = "30000"),//
		// @TestProperties(name = EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "buffer.memory",
		// value = "33554432"),//

		// Ambeth
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "orm.xml"),
		@TestProperties(
				name = LoggerFactory.logLevelPropertyPrefix + '.' + "com.koch.ambeth.event.kafka",
				value = "INFO")})
public class DataChangeTest extends AbstractIocTest {
	public static class KafkaTestModule implements IInitializingModule {
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
			beanContextFactory.registerBean(ClasspathScanner.class).autowireable(IClasspathScanner.class);
		}
	}

	@LogInstance
	private ILogger log;

	@Rule
	public AmbethKafkaJunitRule kafkaRule = new AmbethKafkaJunitRule();

	@Autowired
	protected IProperties properties;

	@Test
	public void test() throws Throwable {
		Properties props = new Properties(properties);

		props.put(AmbethKafkaConfiguration.buildAmbethProperty(AmbethKafkaConfiguration.ZOOKEEPER_URL),
				kafkaRule.zookeeperConnectionString());
		// props.put("group.id", "group");
		props.put(AmbethKafkaConfiguration.buildAmbethProperty(AmbethKafkaConfiguration.BROKER_URL),
				"localhost:" + kafkaRule.kafkaBrokerPort());
		// props.put(EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "zookeeper.session.timeout.ms",
		// "400");
		// props.put(EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "zookeeper.sync.time.ms", "200");
		// props.put(EventToKafkaPublisher.AMBETH_KAFKA_PROP_PREFIX + "auto.commit.interval.ms",
		// "1000");

		IAmbethApplication app1 = createAmbethKafkaApp(props);
		try {
			IAmbethApplication app2 = createAmbethKafkaApp(props);
			try {
				testContexts(app1.getApplicationContext(), app2.getApplicationContext());
			}
			finally {
				app2.close();
			}
		}
		finally {
			app1.close();
		}
	}

	protected IAmbethApplication createAmbethKafkaApp(IProperties props) {
		return Ambeth.createBundle(InformationBus.class)
				.withAmbethModules(EventKafkaModule.class, DataChangeKafkaModule.class,
						KafkaTestModule.class, XmlModule.class)
				.withoutPropertiesFileSearch().withProperties(props).start();
	}

	protected Object lookupCacheEntry(IRootCache rootCache, TestEntity testEntity) {
		return rootCache.getObject(
				new ObjRef(TestEntity.class, ObjRef.PRIMARY_KEY_INDEX, testEntity.getId(), null),
				EnumSet.of(CacheDirective.FailEarly, CacheDirective.LoadContainerResult));
	}

	private void testContexts(IServiceContext left, IServiceContext right) throws Throwable {
		// simple setup: cache a dummy entity and fire from a foreign source an invalidating DCE for
		// that entity
		// on success the cached entity should not be there any more

		int messageCount = 1;
		final ParamHolder<Integer> chunkCountPH = new ParamHolder<>(0);
		final CountDownLatch latch = new CountDownLatch(1);
		IRootCache leftRootCache;
		TestEntity testEntity;
		{
			IEntityFactory entityFactory = left.getService(IEntityFactory.class);
			IEntityMetaDataProvider entityMetaDataProvider =
					left.getService(IEntityMetaDataProvider.class);
			leftRootCache = left.getService(CacheModule.COMMITTED_ROOT_CACHE, IRootCache.class);

			// create cache entry in "left"
			testEntity = entityFactory.createEntity(TestEntity.class);
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(TestEntity.class);
			metaData.getIdMember().setIntValue(testEntity, 1);
			metaData.getVersionMember().setIntValue(testEntity, 1);

			leftRootCache.put(testEntity);

			Assert.assertNotNull(lookupCacheEntry(leftRootCache, testEntity));

			left.getService(IEventListenerExtendable.class).registerEventListener(new IEventListener() {
				@Override
				public void handleEvent(Object eventObject, long dispatchTime, long sequenceId)
						throws Exception {
					if (eventObject instanceof IDataChange && !((IDataChange) eventObject).isLocalSource()) {
						chunkCountPH.setValue(chunkCountPH.getValue().intValue() + 1);
						latch.countDown();
					}
				}
			});
		}
		{
			IEventDispatcher eventDispatcher = right.getService(IEventDispatcher.class);

			// eventDispatcher.enableEventQueue();
			try {
				log.debug("Publish Start: " + System.currentTimeMillis());
				// fire the DCE in "right"
				for (int a = messageCount; a-- > 0;) {
					DataChangeEvent dce = DataChangeEvent.create(0, 1, 0);
					dce.getUpdates().add(new DataChangeEntry(TestEntity.class, ObjRef.PRIMARY_KEY_INDEX,
							testEntity.getId(), testEntity.getVersion() + 1));
					eventDispatcher.dispatchEvent(dce);
				}
				log.debug("Publish End: " + System.currentTimeMillis());
			}
			finally {
				// eventDispatcher.flushEventQueue();
			}
		}
		latch.await(60000, TimeUnit.MILLISECONDS); // maybe 100ms can be fully sufficient but just to be
																								// sure...
		log.debug("Chunk Count: " + chunkCountPH.getValue());

		{
			// ensure that entry in "left" is removed
			Assert.assertNull(lookupCacheEntry(leftRootCache, testEntity));
		}
	}
}
