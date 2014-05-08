using System;
using System.Collections.Generic;
using System.Threading;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Ioc.Hierarchy;
using De.Osthus.Ambeth.Util;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace De.Osthus.Ambeth.Test.ambeth.ioc
{
    [TestClass]
    public class ChildContextHandleTest
    {
        public static readonly String SINGLETON_CCH = "singletonChildContextHandle";

        public static readonly String THREADLOCAL_CCH = "threadlocalChildContextHandle";

        public static readonly String PROTOTYPE_CCH = "prototypeChildContextHandle";

        public static readonly String SINGLETON_CCH_WITH_CCF = "singletonChildContextHandle_with_childContextFactory";

        public static readonly String CHILD_CONTEXT_FACTORY = "childContextFactory";

        public class ChildContextHandleTestModule : IInitializingModule
        {
            public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
            {
                beanContextFactory.RegisterBean<SingletonContextHandle>(SINGLETON_CCH).PropertyValue("Content", new RegisterPhaseDelegate(new DummyListTestModule().AfterPropertiesSet));
                beanContextFactory.RegisterBean<ThreadLocalContextHandle>(THREADLOCAL_CCH).PropertyValue("Content", new RegisterPhaseDelegate(new DummyListTestModule().AfterPropertiesSet));
                beanContextFactory.RegisterBean<PrototypeContextHandle>(PROTOTYPE_CCH).PropertyValue("Content", new RegisterPhaseDelegate(new DummyListTestModule().AfterPropertiesSet));

                beanContextFactory.RegisterBean<SingletonContextHandle>(SINGLETON_CCH_WITH_CCF).PropertyRef("ContextFactory", CHILD_CONTEXT_FACTORY);
            }
        }

        public class DummyListTestModule : IInitializingModule
        {
            public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
            {
                List<int> testList = new List<int>();
                testList.Add(1);
                testList.Add(2);
                beanContextFactory.RegisterWithLifecycle(testList).Autowireable<IList<int>>();
            }
        }

        public class ChildContextHandleChildFactoryTestModule : IInitializingModule
        {
            public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
            {
                beanContextFactory.RegisterBean<DefaultChildContextFactory>(CHILD_CONTEXT_FACTORY).PropertyValue("Modules", typeof(DummyListTestModule));
            }
        }

        protected IServiceContext beanContext;

        [TestInitialize]
        public virtual void SetUp()
        {
            ParamChecker.AssertNull(beanContext, "Must never happen");

            beanContext = BeanContextFactory.CreateBootstrap(typeof(ChildContextHandleChildFactoryTestModule));
            beanContext = beanContext.CreateService(typeof(ChildContextHandleTestModule));
        }

        [TestCleanup]
        public virtual void TearDown()
        {
            if (beanContext != null)
            {
                IServiceContext tempBeanContext = beanContext;
                beanContext = null;
                tempBeanContext.GetRoot().Dispose();
            }
        }

        [TestMethod]
        public void ChildContextHandle_Singleton()
        {
            IContextHandle singletonCCH = beanContext.GetService<IContextHandle>(SINGLETON_CCH);

            IServiceContext singletonChildContext1 = singletonCCH.Start();
            IServiceContext singletonChildContext2 = singletonCCH.Start();

            Assert.AreSame(singletonChildContext1, singletonChildContext2);

            singletonCCH.Stop();
            Assert.IsTrue(singletonChildContext1.IsDisposed);
        }

        [TestMethod]
        public void ChildContextHandle_ThreadLocal()
        {
            IContextHandle threadLocalCCH = beanContext.GetService<IContextHandle>(THREADLOCAL_CCH);

            IServiceContext threadLocalChildContext1 = threadLocalCCH.Start();
            IServiceContext threadLocalChildContext2 = threadLocalCCH.Start();

            Assert.AreSame(threadLocalChildContext1, threadLocalChildContext2);

            CountdownEvent countDownLatch = new CountdownEvent(1);

            ThreadPool.QueueUserWorkItem(delegate(Object state)
            {
                IServiceContext threadLocalChildContext1_2 = threadLocalCCH.Start();
                IServiceContext threadLocalChildContext2_2 = threadLocalCCH.Start();

                Assert.AreSame(threadLocalChildContext1_2, threadLocalChildContext2_2);
                Assert.AreNotSame(threadLocalChildContext1_2, threadLocalChildContext1);

                countDownLatch.Signal();
                threadLocalCCH.Stop();
                Assert.IsTrue(threadLocalChildContext1_2.IsDisposed);
                Assert.IsFalse(threadLocalChildContext1.IsDisposed);
            }, null);

            if (!countDownLatch.Wait(TimeSpan.FromMilliseconds(5000)))
            {
                throw new TimeoutException("Thread did not respond");
            }
            Assert.IsFalse(threadLocalChildContext1.IsDisposed);
            threadLocalCCH.Stop();
            Assert.IsTrue(threadLocalChildContext1.IsDisposed);
        }

        [TestMethod]
        public void ChildContextHandle_Prototype()
        {
            IContextHandle prototypeCCH = beanContext.GetService<IContextHandle>(PROTOTYPE_CCH);

            IServiceContext prototypeChildContext1 = prototypeCCH.Start();
            IServiceContext prototypeChildContext2 = prototypeCCH.Start();

            Assert.AreNotSame(prototypeChildContext1, prototypeChildContext2);

            prototypeCCH.Stop();
            Assert.IsTrue(prototypeChildContext1.IsDisposed);
            Assert.IsTrue(prototypeChildContext2.IsDisposed);
        }

        [TestMethod]
        public void ChildContextHandle_With_Factory_Singleton()
        {
            IContextHandle singletonCCH = beanContext.GetService<IContextHandle>(SINGLETON_CCH_WITH_CCF);

            IServiceContext childContext = singletonCCH.Start();

            IList<int> testList = childContext.GetService<IList<int>>();
            Assert.AreEqual(testList.Count, 2);
        }
    }
}
