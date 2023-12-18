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

import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.cache.ioc.CacheModule;
import com.koch.ambeth.core.Ambeth;
import com.koch.ambeth.datachange.kafka.ioc.DataChangeKafkaModule;
import com.koch.ambeth.datachange.transfer.DataChangeEntry;
import com.koch.ambeth.datachange.transfer.DataChangeEvent;
import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.event.IEventListener;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.event.kafka.AmbethKafkaConfiguration;
import com.koch.ambeth.event.kafka.AmbethKafkaJUnitRuleLegacy;
import com.koch.ambeth.event.kafka.config.EventKafkaConfigurationConstants;
import com.koch.ambeth.event.kafka.ioc.EventKafkaModule;
import com.koch.ambeth.informationbus.InformationBus;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
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
import com.koch.ambeth.testutil.category.PerformanceTests;
import com.koch.ambeth.util.IClasspathScanner;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.function.CheckedRunnable;
import com.koch.ambeth.xml.ioc.XmlModule;
import com.koch.ambeth.xml.util.ClasspathScanner;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@TestPropertiesList({
        // producer
        @TestProperties(name = EventKafkaConfigurationConstants.TOPIC_NAME, value = "test"), //
        @TestProperties(name = AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "acks", value = "all"), //
        @TestProperties(name = AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "retries", value = "0"), //
        @TestProperties(name = AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "batch.size", value = "16384"), //
        @TestProperties(name = AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "linger.ms", value = "1"), //
        @TestProperties(name = AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "buffer.memory", value = "33554432"), //

        // consumer
        @TestProperties(name = AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "group.id", value = "groupId"), //
        @TestProperties(name = AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "enable.auto.commit", value = "true"), //
        @TestProperties(name = AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "auto.commit.interval.ms", value = "1"), //
        @TestProperties(name = AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "session.timeout.ms", value = "30000"), //
        @TestProperties(name = AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + "buffer.memory", value = "33554432"),//
        // @TestProperties(name = "ambeth.log.level", value = "DEBUG"),//
})
@Category(PerformanceTests.class)
public class MassDataChangeTest extends AbstractIocTest {
    private static final int NUM_ENTITIES = 1000;

    public static FutureTask<String> future = new FutureTask<>(() -> null);

    @Rule
    public AmbethKafkaJUnitRuleLegacy kafkaRule = new AmbethKafkaJUnitRuleLegacy(this);
    @Autowired
    protected IProperties properties;
    @LogInstance
    private ILogger log;

    @Test
    public void test() throws Throwable {
        var props = new Properties(properties);

        for (var entry : kafkaRule.producerConfig(StringSerializer.class.getName()).originals().entrySet()) {
            props.put(AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + entry.getKey(), entry.getValue());
        }
        for (var entry : kafkaRule.consumerConfig(StringDeserializer.class.getName()).originals().entrySet()) {
            props.put(AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + entry.getKey(), entry.getValue());
        }
        props.put(ServiceConfigurationConstants.mappingFile, "orm.xml");

        var app1 = Ambeth.createBundle(InformationBus.class)
                         .withFrameworkModules(EventKafkaModule.class, DataChangeKafkaModule.class, KafkaTestModule.class, XmlModule.class)
                         .withoutPropertiesFileSearch()
                         .withProperties(props)
                         .start();
        try {
            var app2 = Ambeth.createBundle(InformationBus.class)
                             .withFrameworkModules(EventKafkaModule.class, DataChangeKafkaModule.class, KafkaTestModule.class, XmlModule.class)
                             .withoutPropertiesFileSearch()
                             .withProperties(props)
                             .start();
            try {
                // Measure processing time with Kafka
                long kafkaTime = testDataChangeEventsWithKafka(app1.getApplicationContext(), app2.getApplicationContext(), NUM_ENTITIES);
                // Measure processing time without Kafka
                long withoutKafkaTime = testContextsWithout(app1.getApplicationContext(), app2.getApplicationContext(), NUM_ENTITIES);
                log.info("Kafka - processing time:" + (kafkaTime - withoutKafkaTime) + "ms " + Math.round(kafkaTime / (withoutKafkaTime / 100) - 100) + " %");
            } finally {
                app2.close();
            }
        } finally {
            app1.close();
        }
    }

    protected Object lookupCacheEntry(IRootCache rootCache, TestEntity testEntity) {
        return rootCache.getObject(new ObjRef(TestEntity.class, ObjRef.PRIMARY_KEY_INDEX, testEntity.getId(), null), EnumSet.of(CacheDirective.FailEarly, CacheDirective.LoadContainerResult));
    }

    /**
     * fill up the cache with entities
     *
     * @param left
     * @param countEntities
     * @param testEntityList
     */
    private void fillCache(IServiceContext left, int countEntities, final List<TestEntity> testEntityList) {
        IRootCache leftRootCache;
        {
            TestEntity testEntity;
            for (int i = countEntities; i > 0; i--) {

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

    private long testDataChangeEventsWithKafka(IServiceContext left, final IServiceContext right, int countEntities) throws Throwable {
        final List<TestEntity> testEntityList = new ArrayList<>();
        right.createService(CounterModule.class);
        fillCache(left, countEntities, testEntityList);
        long millis = System.currentTimeMillis();

        {
            final CheckedRunnable worker = () -> {
                // fire the DCE in "right"
                for (Iterator<TestEntity> iterator = testEntityList.iterator(); iterator.hasNext(); ) {
                    TestEntity testEntity = iterator.next();
                    DataChangeEvent dce = DataChangeEvent.create(0, 1, 0);
                    dce.getUpdates().add(new DataChangeEntry(TestEntity.class, ObjRef.PRIMARY_KEY_INDEX, testEntity.getId(), testEntity.getVersion() + 1));
                    right.getService(IEventDispatcher.class).dispatchEvent(dce);
                }
            };
            new Thread(() -> CheckedRunnable.invoke(worker)).start();
        }
        log.info("start waiting...");
        // wait for all DCE events to arrive in the right context
        // TODO: is this correct?
        future.get(2, TimeUnit.MINUTES);

        long timeSpend = System.currentTimeMillis() - millis;
        Thread.sleep(2000); // TODO: this should not be needed because the feature "should" only be
        // return if entities have been removed from the cache
        log.info("Kafka DCE for " + NUM_ENTITIES + " DCEs took: " + timeSpend + "ms");
        ensureCacheCleaned(left, testEntityList);
        return timeSpend;
    }

    private void ensureCacheCleaned(IServiceContext left, final List<TestEntity> testEntityList) {
        IRootCache leftRootCache;
        {
            // ensure that entry in "left" is removed
            leftRootCache = left.getService(CacheModule.COMMITTED_ROOT_CACHE, IRootCache.class);

            for (Iterator<TestEntity> iterator = testEntityList.iterator(); iterator.hasNext(); ) {
                TestEntity testEntity = iterator.next();

                Assert.assertNull("Expect entitiy: " + testEntity.getId() + " to be removed from the cache.", lookupCacheEntry(leftRootCache, testEntity));
            }
        }
    }

    private long testContextsWithout(IServiceContext left, final IServiceContext right1, int countEntities) throws Throwable {
        IRootCache leftRootCache;
        final List<TestEntity> testEntityList = new ArrayList<>();
        fillCache(left, countEntities, testEntityList);
        long mills = System.currentTimeMillis();
        log.info("start waiting...");
        {
            // fire the DCE in "right"
            for (Iterator<TestEntity> iterator = testEntityList.iterator(); iterator.hasNext(); ) {
                TestEntity testEntity = iterator.next();
                DataChangeEvent dce = DataChangeEvent.create(0, 1, 0);
                dce.getUpdates().add(new DataChangeEntry(TestEntity.class, ObjRef.PRIMARY_KEY_INDEX, testEntity.getId(), testEntity.getVersion() + 1));
                left.getService(IEventDispatcher.class).dispatchEvent(dce);
            }
        }
        long timeSpend = System.currentTimeMillis() - mills;
        Thread.sleep(2000); // TODO: this should not be needed because the feature "should" only be
        // return if entities have been removed from the cache

        log.info("Without Kafka DCE for " + NUM_ENTITIES + " DCEs took: " + timeSpend + "ms");
        ensureCacheCleaned(left, testEntityList);
        return timeSpend;
    }

    public static class KafkaTestModule implements IInitializingModule {
        @Override
        public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
            beanContextFactory.registerBean(ClasspathScanner.class).autowireable(IClasspathScanner.class);
        }
    }

    public static class CounterModule implements IInitializingModule {

        @Override
        public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {

            IBeanConfiguration registerExternalBean = beanContextFactory.registerExternalBean(new IEventListener() {

                private int globalCounter = 0;

                @Override
                public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) throws Exception {
                    // wait until all DCE's arrive in the "right" cache.
                    if (globalCounter >= NUM_ENTITIES - 1) {
                        future.run();
                    }
                    globalCounter++;

                }
            });
            beanContextFactory.link(registerExternalBean).to(IEventListenerExtendable.class).with(DataChangeEvent.class);
        }
    }
}
