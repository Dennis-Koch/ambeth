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
import com.koch.ambeth.core.Ambeth;
import com.koch.ambeth.core.start.IAmbethApplication;
import com.koch.ambeth.informationbus.InformationBus;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.accessor.AbstractAccessor;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.log.LoggerFactory;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.testutil.category.SlowTests;
import com.koch.ambeth.util.IClasspathScanner;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.xml.util.ClasspathScanner;
import lombok.Data;
import lombok.SneakyThrows;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.jar.asm.MethodVisitor;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

@Category(SlowTests.class)
@TestPropertiesList({
        //@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "orm-member-access-performance.xml"),
        @TestProperties(name = LoggerFactory.logLevelPropertyPrefix + '.' + "com.koch.ambeth.event.kafka", value = "INFO")
})
public class MemberAccessPerformanceTest extends AbstractIocTest {
    @Autowired
    IProperties properties;
    @LogInstance
    ILogger log;

    @Test
    public void test() throws Throwable {
        var props = new Properties(properties);

        try (var leftApp = createAmbethApp(props)) {
            testContexts(leftApp.getApplicationContext());
        }
    }

    protected IAmbethApplication createAmbethApp(IProperties props) {
        return Ambeth.createEmptyBundle(InformationBus.class)
                     //.withFrameworkModules(XmlModule.class)
                     .withoutPropertiesFileSearch().withProperties(props).start();
    }

    protected Object lookupCacheEntry(IRootCache rootCache, TestEntity testEntity) {
        return rootCache.getObject(new ObjRef(TestEntity.class, ObjRef.PRIMARY_KEY_INDEX, testEntity.getId(), null), EnumSet.of(CacheDirective.FailEarly, CacheDirective.LoadContainerResult));
    }

    @Data
    public static class A {
        B b;
    }

    @Data
    public static class B {
        C c;
    }

    @Data
    public static class C {
        D d;
    }

    @Data
    public static class D {

    }
    
    enum StackManipulations implements StackManipulation {
        INSTANCE;

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public Size apply(MethodVisitor methodVisitor, Implementation.Context implementationContext) {

            return null;
        }
    }

    @SneakyThrows
    private void testContexts(IServiceContext left) {
    }

    public static class KafkaTestModule implements IInitializingModule {

        @Override
        public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
            beanContextFactory.registerBean(ClasspathScanner.class).autowireable(IClasspathScanner.class);
        }
    }
}
