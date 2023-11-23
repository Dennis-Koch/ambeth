package com.koch.ambeth.ioc.annotation;

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

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.IocModule;
import com.koch.ambeth.ioc.exception.BeanContextInitException;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestRebuildContext;
import org.junit.Assert;
import org.junit.Test;

@TestRebuildContext
public class AutowiredTest extends AbstractIocTest {
    public static final String fromContextName1 = "otherContext1", fromContextName2 = "otherContext2";
    public static final String bean1Name = "bean1", bean2Name = "bean2", bean3Name = "bean3", bean4Name = "bean4";

    @Test
    @TestModule(AutowiredTestModule.class)
    public void testAutowired() {
        Bean1 bean1 = beanContext.getService(bean1Name, Bean1.class);
        Assert.assertNull(bean1.bean2);
        Assert.assertNotNull(bean1.bean2Autowired);
        Assert.assertNull(bean1.bean3);
        Assert.assertNull(bean1.bean3Autowired);
    }

    @Test(expected = BeanContextInitException.class)
    public void testAutowiredNotOptional() {
        beanContext.createService(AutowiredTestModule2.class);
    }

    @Test
    public void testAutowiredOptional() {
        IServiceContext beanContext = this.beanContext.createService(AutowiredTestModule3.class);
        try {
            Bean1 bean1 = beanContext.getService(bean1Name, Bean1.class);
            Assert.assertNull(bean1.bean2);
            Assert.assertNotNull(bean1.bean2Autowired);
            Assert.assertNull(bean1.bean3);
            Assert.assertNull(bean1.bean3Autowired);
        } finally {
            beanContext.dispose();
        }
    }

    @Test
    public void testAutowiredByName() {
        IServiceContext beanContext = this.beanContext.createService(AutowiredTestModule4.class);
        try {
            Bean1 bean1 = beanContext.getService(bean1Name, Bean1.class);
            Assert.assertNull(bean1.bean2);
            Assert.assertNotNull(bean1.bean2Autowired);
            Assert.assertNull(bean1.bean3);
            Assert.assertNull(bean1.bean3Autowired);
        } finally {
            beanContext.dispose();
        }
    }

    @Test
    @TestModule(AutowiredTestModule.class)
    public void autowiredVisibility() {
        AutowiredTestBean bean = beanContext.getService(AutowiredTestBean.class);
        Assert.assertSame(beanContext, bean.getBeanContextPrivate());
        Assert.assertNull(bean.getBeanContextPrivateSetter());
        Assert.assertSame(beanContext, bean.getBeanContextPrivateSetterAutowired());

        Assert.assertSame(beanContext, bean.getBeanContextProtected());
        Assert.assertNull(bean.getBeanContextProtectedSetter());
        Assert.assertSame(beanContext, bean.getBeanContextProtectedSetterAutowired());

        Assert.assertSame(beanContext, bean.getBeanContextPublic());
        Assert.assertSame(beanContext, bean.getBeanContextPublicSetter());
    }

    @Test
    @TestModule(AutowiredTestModule.class)
    public void testAutowiredFromContext() {
        final String bean5Name_1 = "bean5Name_1";
        final String bean5Name_2 = "bean5Name_2";

        final IServiceContext otherContext2 = BeanContextFactory.createBootstrap(IocModule.class).createService(AutowiredTestModule.class);
        try {
            IServiceContext otherContext = BeanContextFactory.createBootstrap(IocModule.class).createService(state -> {
                state.registerExternalBean(fromContextName1, beanContext);
                state.registerExternalBean(fromContextName2, otherContext2);

                state.registerBean(bean5Name_1, Bean5.class);

                state.registerBean(bean5Name_2, Bean5.class).propertyRefFromContext("Bean1AutowiredForeignContext", fromContextName2, bean1Name);
            }, AutowiredTestModule.class);
            try {
                Bean5 bean5_1 = otherContext.getService(bean5Name_1, Bean5.class);
                Bean5 bean5_2 = otherContext.getService(bean5Name_2, Bean5.class);

                Bean1 bean1 = beanContext.getService(bean1Name, Bean1.class);
                Bean1 otherContext1_bean1 = otherContext.getService(bean1Name, Bean1.class);
                Bean1 otherContext2_bean1 = otherContext2.getService(bean1Name, Bean1.class);

                Assert.assertNotSame(bean1, otherContext1_bean1);
                Assert.assertNotSame(bean1, otherContext2_bean1);
                Assert.assertNotSame(otherContext1_bean1, otherContext2_bean1);

                Assert.assertSame(otherContext1_bean1, bean5_1.bean1Autowired);
                Assert.assertSame(bean1, bean5_1.bean1AutowiredForeignContext);

                Assert.assertSame(otherContext1_bean1, bean5_2.bean1Autowired);
                Assert.assertNotSame(otherContext2_bean1, bean5_2.bean1Autowired);
                Assert.assertSame(otherContext2_bean1, bean5_2.bean1AutowiredForeignContext);
            } finally {
                otherContext.getRoot().dispose();
            }
        } finally {
            otherContext2.dispose();
        }
    }

    public static class Bean1 {
        protected Bean2 bean2;

        @Autowired
        protected Bean2 bean2Autowired;

        protected Bean3 bean3;

        @Autowired(optional = true)
        protected Bean3 bean3Autowired;
    }

    public static class Bean2 {
        // Intended blank
    }

    public static class Bean3 {
        // Intended blank
    }

    public static class Bean4 {
        @Autowired(bean1Name)
        protected Bean1 bean1Autowired;
    }

    public static class Bean5 {
        @Autowired(value = bean1Name, fromContext = fromContextName1)
        protected Bean1 bean1AutowiredForeignContext;

        @Autowired(value = bean1Name)
        protected Bean1 bean1Autowired;
    }

    public static class AutowiredTestModule implements IInitializingModule {
        @Override
        public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
            beanContextFactory.registerBean(bean1Name, Bean1.class);
            beanContextFactory.registerBean(bean2Name, Bean2.class).autowireable(Bean2.class);
            beanContextFactory.registerBean(bean3Name, Bean3.class);

            beanContextFactory.registerBean(AutowiredTestBean.class).autowireable(AutowiredTestBean.class);
        }
    }

    public static class AutowiredTestModule2 implements IInitializingModule {
        @Override
        public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
            beanContextFactory.registerBean(bean1Name, Bean1.class);
            beanContextFactory.registerBean(bean3Name, Bean3.class);
        }
    }

    public static class AutowiredTestModule3 implements IInitializingModule {
        @Override
        public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
            beanContextFactory.registerBean(bean1Name, Bean1.class);
            beanContextFactory.registerBean(bean2Name, Bean2.class).autowireable(Bean2.class);
        }
    }

    public static class AutowiredTestModule4 implements IInitializingModule {
        @Override
        public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
            beanContextFactory.registerBean(bean1Name, Bean1.class);
            beanContextFactory.registerBean(bean2Name, Bean2.class).autowireable(Bean2.class);
            beanContextFactory.registerBean(bean4Name, Bean4.class);
        }
    }
}
