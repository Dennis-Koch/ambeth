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
import com.koch.ambeth.core.start.IAmbethApplication;
import com.koch.ambeth.datachange.kafka.ioc.DataChangeKafkaModule;
import com.koch.ambeth.datachange.model.IDataChange;
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
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.testutil.category.SlowTests;
import com.koch.ambeth.util.IClasspathScanner;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.xml.ioc.XmlModule;
import com.koch.ambeth.xml.util.ClasspathScanner;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Category(SlowTests.class)
@TestPropertiesList({
        // producer
        @TestProperties(name = EventKafkaConfigurationConstants.TOPIC_NAME, value = "test"),
        // consumer
        @TestProperties(name = AmbethKafkaConfiguration.AMBETH_KAFKA_PROP_PREFIX + AmbethKafkaConfiguration.CONS_GROUP_ID, value = "groupId"),
        // Ambeth
        @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "orm.xml"),
        @TestProperties(name = LoggerFactory.logLevelPropertyPrefix + '.' + "com.koch.ambeth.event.kafka", value = "INFO")
})
public class DataChangeTest extends AbstractIocTest {

    @Rule
    public AmbethKafkaJUnitRuleLegacy kafkaRule = new AmbethKafkaJUnitRuleLegacy(this);
    @Autowired
    protected IProperties properties;
    @LogInstance
    private ILogger log;

    @Test
    public void test() throws Throwable {
        var props = new Properties(properties);

        props.put(AmbethKafkaConfiguration.buildAmbethProperty(AmbethKafkaConfiguration.BROKER_URL), kafkaRule.kafkaBootstrapServers());

        try (var leftApp = createAmbethKafkaApp(props); var rightApp = createAmbethKafkaApp(props)) {
            testContexts(leftApp.getApplicationContext(), rightApp.getApplicationContext());
        }
    }

    protected IAmbethApplication createAmbethKafkaApp(IProperties props) {
        return Ambeth.createEmptyBundle(InformationBus.class)
                     .withFrameworkModules(EventKafkaModule.class, DataChangeKafkaModule.class, KafkaTestModule.class, XmlModule.class)
                     .withoutPropertiesFileSearch()
                     .withProperties(props)
                     .start();
    }

    protected Object lookupCacheEntry(IRootCache rootCache, TestEntity testEntity) {
        return rootCache.getObject(new ObjRef(TestEntity.class, ObjRef.PRIMARY_KEY_INDEX, testEntity.getId(), null), EnumSet.of(CacheDirective.FailEarly, CacheDirective.LoadContainerResult));
    }

    private void testContexts(IServiceContext left, IServiceContext right) throws Throwable {
        // simple setup: cache a dummy entity and fire from a foreign source an invalidating DCE for that entity
        // on success the cached entity should not be there any more

        var messageCount = 1;
        var chunkCountPH = new AtomicInteger();
        var latch = new CountDownLatch(1);
        IRootCache leftRootCache;
        TestEntity testEntity, testEntity2;
        {
            var entityFactory = left.getService(IEntityFactory.class);
            var entityMetaDataProvider = left.getService(IEntityMetaDataProvider.class);
            leftRootCache = left.getService(CacheModule.COMMITTED_ROOT_CACHE, IRootCache.class);

            // create cache entry in "left"
            testEntity = entityFactory.createEntity(TestEntity.class);
            testEntity2 = entityFactory.createEntity(TestEntity.class);
            var metaData = entityMetaDataProvider.getMetaData(TestEntity.class);
            metaData.getIdMember().setIntValue(testEntity, 1);
            metaData.getVersionMember().setIntValue(testEntity, 1);
            testEntity.setStringValue1(new StringBuilder("hugo").toString());

            metaData.getIdMember().setIntValue(testEntity2, 2);
            metaData.getVersionMember().setIntValue(testEntity2, 1);
            testEntity2.setStringValue1(new StringBuilder("hugo").toString());

            leftRootCache.put(List.of(testEntity, testEntity2));

            assertThat(lookupCacheEntry(leftRootCache, testEntity)).isNotNull();
            assertThat(lookupCacheEntry(leftRootCache, testEntity2)).isNotNull();

            Object cacheValue = leftRootCache.getObject(TestEntity.class, testEntity.getId(), CacheDirective.cacheValueResult());
            Object cacheValue2 = leftRootCache.getObject(TestEntity.class, testEntity2.getId(), CacheDirective.cacheValueResult());


            left.link((IEventListener) (eventObject, dispatchTime, sequenceId) -> {
                if (eventObject instanceof IDataChange && !((IDataChange) eventObject).isLocalSource()) {
                    chunkCountPH.incrementAndGet();
                    latch.countDown();
                }
            }).to(IEventListenerExtendable.class).finishLink();
        }
        {
            var eventDispatcher = right.getService(IEventDispatcher.class);

            // eventDispatcher.enableEventQueue();
            try {
                log.debug("Publish Start: " + System.currentTimeMillis());
                // fire the DCE in "right"
                for (int a = messageCount; a-- > 0; ) {
                    var dce = DataChangeEvent.create(0, 1, 0);
                    dce.getUpdates().add(new DataChangeEntry(TestEntity.class, ObjRef.PRIMARY_KEY_INDEX, testEntity.getId(), testEntity.getVersion() + 1));
                    eventDispatcher.dispatchEvent(dce);
                }
                log.debug("Publish End: " + System.currentTimeMillis());
            } finally {
                // eventDispatcher.flushEventQueue();
            }
        }
        // maybe 100ms can be fully sufficient but just to be
        if (!latch.await(60, TimeUnit.SECONDS)) {
            throw new TimeoutException();
        }
        // sure...
        log.debug("Chunk Count: " + chunkCountPH.get());

        {
            // ensure that entry in "left" is removed
            assertThat(lookupCacheEntry(leftRootCache, testEntity)).isNull();
        }
    }

    public static class KafkaTestModule implements IInitializingModule {

        @Override
        public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
          //  beanContextFactory.registerBean(ClasspathScanner.class).autowireable(IClasspathScanner.class);
        }
    }
}
