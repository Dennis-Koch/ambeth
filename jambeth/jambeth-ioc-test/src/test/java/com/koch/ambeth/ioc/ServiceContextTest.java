package com.koch.ambeth.ioc;

/*-
 * #%L
 * jambeth-ioc-test
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

import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.util.SystemUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class ServiceContextTest {
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    private ServiceContext beanContext;
    private ServiceContext childBeanContext;

    @Before
    public void setUp() throws Exception {
        beanContext = (ServiceContext) BeanContextFactory.createBootstrap(Properties.getApplication());
        childBeanContext = (ServiceContext) beanContext.createService("child");
    }

    @After
    public void tearDown() throws Exception {
        if (beanContext != null) {
            beanContext.dispose();
        }
    }

    @Test
    @Ignore
    public final void testDefaultServiceProviderIThreadLocalObjectCollector() {
        assertNotNull(beanContext);
    }

    @Test
    @Ignore
    public final void testDefaultServiceProviderDefaultServiceProviderIThreadLocalObjectCollector() {
        assertNotNull(childBeanContext);
    }

    @Test
    public final void testGetParent() {
        assertNull(beanContext.getParent());
        assertSame(beanContext, childBeanContext.getParent());
    }

    @Test
    public final void testGetPostProcessors() {
        List<IBeanPostProcessor> actual = beanContext.getPostProcessors();
        assertTrue(actual == null || actual.isEmpty());
    }

    @Test
    public final void testSetPostProcessors() {
        List<IBeanPostProcessor> expected = new ArrayList<>();
        expected.add(new IBeanPostProcessor() {
            @Override
            public Object postProcessBean(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Class<?> beanType, Object targetBean,
                    Set<Class<?>> requestedTypes) {
                return null;
            }
        });
        beanContext.addPostProcessor(expected.get(0));
        List<IBeanPostProcessor> actuals = beanContext.getPostProcessors();
        assertEquals(expected.size(), actuals.size());
        for (IBeanPostProcessor actual : actuals) {
            assertTrue(expected.contains(actual));
        }
    }

    // TODO Fehlende Tests implementieren
    @Test
    @Ignore
    public final void testAddMapping() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testCheckNotDisposed() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testCheckConfigurable() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testCheckNotInitializable() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testGetServiceExtendable() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testGetServiceProvider() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testGetPreProcessors() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testDispose() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testCreateServiceClassOfQArray() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testCreateServiceRegisterPhaseDelegateClassOfQArray() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testCreateServiceClassOfIRegisterPhaseDelegateClassOfQArray() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testHandleObjects() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testFinishConfiguration() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testGetServiceClassOfT() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testGetServiceIntern() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testGetServiceString() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testApplyServicesToObject() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testApplyServiceToObject() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testApplyLocalServices() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testLinkClassOfRClassOfLClassOfTObjectArray() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testLinkClassOfRClassOfLClassOfTObjectArrayObjectArray() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testLinkClassOfRClassOfLT() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testLinkClassOfRClassOfLTObjectArray() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testLinkClassOfRClassOfLRT() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testLinkClassOfRClassOfLRTObjectArray() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testRegisterServiceStringObject() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testRegisterServiceClassOfT() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testRegisterServiceClassOfIClassOfT() {
        IServiceContext beanContext = this.beanContext.createService("test", childContextFactory -> {
            IBeanConfiguration actual = childContextFactory.registerAutowireableBean(IRegisterableServiceFake.class, RegisterableServiceFake1.class);
            assertNotNull("Instantiation failed!", actual);

            PrintStream orgOut = System.out;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            System.setOut(new PrintStream(out));
            IBeanConfiguration actual2 = childContextFactory.registerAutowireableBean(IRegisterableServiceFake.class, RegisterableServiceFake1.class);
            System.setOut(orgOut);

            String expectedLogEnd =
                    "The service of type '" + RegisterableServiceFake1.class.getName() + "' is already mapped to type '" + IRegisterableServiceFake.class.getName() + "'. Skipping registration.";
            String actualLog = out.toString().trim();

            assertTrue("Wrong/missing log entry!" + SystemUtil.lineSeparator() + "Actual: " + actualLog + SystemUtil.lineSeparator() + "Should end with: " + expectedLogEnd,
                    actualLog.endsWith(expectedLogEnd));
            assertNotNull("Returned null!", actual2);
            assertSame("New instance!", actual, actual2);

            out = new ByteArrayOutputStream();
            System.setOut(new PrintStream(out));
            assertNull("Should return null!", childContextFactory.registerAutowireableBean(IRegisterableServiceFake.class, RegisterableServiceFake2.class));
            System.setOut(orgOut);

            expectedLogEnd = "There is already a service mapped to type '" + IRegisterableServiceFake.class.getName() + "' of type '" + ServiceContextTest.this.beanContext.typeToServiceDict.get(
                    IRegisterableServiceFake.class).getClass().getName() + "'. Skipping registration of type '" + RegisterableServiceFake2.class.getName() + "'";
            actualLog = out.toString().trim();
            assertTrue("Wrong/missing log entry!" + SystemUtil.lineSeparator() + "Actual: " + actualLog + SystemUtil.lineSeparator() + "Should end with: " + expectedLogEnd,
                    actualLog.endsWith(expectedLogEnd));
        });
        assertEquals("Instantiation failed!", beanContext.getService(IRegisterableServiceFake.class), RegisterableServiceFake1.class);
    }

    @Test
    @Ignore
    public final void testProcessCallbacks() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testPostProcessService() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testPostProcessServiceInstance() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testRegisterServiceClassOfTObject() {
        final IRegisterableServiceFake service1 = new RegisterableServiceFake1();
        final IRegisterableServiceFake service1_2 = new RegisterableServiceFake1();
        final IRegisterableServiceFake service2 = new RegisterableServiceFake2();

        PrintStream orgOut = System.out;
        {
            ServiceContext childContext =
                    (ServiceContext) beanContext.createService("test", childContextFactory -> childContextFactory.registerExternalBean(service1).autowireable(IRegisterableServiceFake.class));

            assertTrue("Registration failed!", childContext.typeToServiceDict.containsKey(IRegisterableServiceFake.class));
            assertNotNull("Registration failed!", childContext.typeToServiceDict.get(IRegisterableServiceFake.class));
            assertSame("Registration failed!", service1, childContext.typeToServiceDict.get(IRegisterableServiceFake.class));
        }

        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            System.setOut(new PrintStream(out));

            beanContext.createService("test", childContextFactory -> {
                childContextFactory.registerExternalBean(service1).autowireable(IRegisterableServiceFake.class);
                childContextFactory.registerExternalBean(service1).autowireable(IRegisterableServiceFake.class);
            });
            System.setOut(orgOut);
            String expectedLogEnd =
                    "The service of type '" + RegisterableServiceFake1.class.getName() + "' is already mapped to type '" + IRegisterableServiceFake.class.getName() + "'. Skipping registration.";
            String actualLog = out.toString().trim();
            assertTrue("Wrong/missing log entry!" + System.getProperty("line.separator") + "Actual: " + actualLog + System.getProperty("line.separator") + "Should end with: " + expectedLogEnd,
                    actualLog.endsWith(expectedLogEnd));
        }
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            System.setOut(new PrintStream(out));

            beanContext.createService("test", childContextFactory -> {
                childContextFactory.registerExternalBean(service1).autowireable(IRegisterableServiceFake.class);
                childContextFactory.registerExternalBean(service1_2).autowireable(IRegisterableServiceFake.class);
            });
            System.setOut(orgOut);
            String expectedLogEnd =
                    "Another service of type '" + RegisterableServiceFake1.class.getName() + "' is already mapped to type '" + IRegisterableServiceFake.class.getName() + "'. Skipping registration.";
            String actualLog = out.toString().trim();
            assertTrue("Wrong/missing log entry!" + System.getProperty("line.separator") + "Actual: " + actualLog + System.getProperty("line.separator") + "Should end with: " + expectedLogEnd,
                    actualLog.endsWith(expectedLogEnd));
            assertSame("Registered new object!", service1, beanContext.typeToServiceDict.get(IRegisterableServiceFake.class));
        }
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            System.setOut(new PrintStream(out));
            beanContext.createService("test", childContextFactory -> {
                childContextFactory.registerExternalBean(service1).autowireable(IRegisterableServiceFake.class);
                childContextFactory.registerExternalBean(service2).autowireable(IRegisterableServiceFake.class);
            });
            System.setOut(orgOut);
            String expectedLogEnd =
                    "There is already a service mapped to type '" + IRegisterableServiceFake.class.getName() + "' of type '" + beanContext.typeToServiceDict.get(IRegisterableServiceFake.class)
                                                                                                                                                            .getClass()
                                                                                                                                                            .getName() + "'. Skipping registration " + "of" + " type '" + RegisterableServiceFake2.class.getName() + "'";
            String actualLog = out.toString().trim();
            assertTrue("Wrong/missing log entry!" + System.getProperty("line.separator") + "Actual: " + actualLog + System.getProperty("line.separator") + "Should end with: " + expectedLogEnd,
                    actualLog.endsWith(expectedLogEnd));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public final void testRegisterServiceClassOfTObject_misfittingObject() {
        beanContext.createService("test", childContextFactory -> {
            childContextFactory.registerBean("fake", Object.class).autowireable(IRegisterableServiceFake.class);
        });
    }

    @Test
    @Ignore
    public final void testRegisterObjectT() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testRegisterObjectClassOfTObjectArray() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testMonitorObject() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testMonitorSimpleObject() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testGetObjects() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testRegisterServiceListenerClassOfQIServiceListener() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testRegisterServiceListenerClassOfQIServiceListenerObjectArrayObjectArray() {
        fail("Not yet implemented");
    }

    @Test
    @Ignore
    public final void testGetAnnotatedObjects() {
        fail("Not yet implemented");
    }
}
