using De.Osthus.Ambeth.Testutil;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Log;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.Collections;

namespace De.Osthus.Ambeth.Ioc.Extendable
{
    [TestClass]
    [TestModule(typeof(ExtendableBeanTestModule))]
    public class ExtendableBeanTest : AbstractIocTest
    {
        public class ExtendableBeanTestModule : IInitializingModule
        {
            public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
            {
                beanContextFactory.registerBean<ExtendableBean>("testExtensionPoint")
                        .propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, typeof(ITestListenerExtendable))
                        .propertyValue(ExtendableBean.P_PROVIDER_TYPE, typeof(ITestListenerRegistry));

                beanContextFactory.registerBean<ExtendableBean>("testExtensionPoint2")
                        .propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, typeof(ITestListenerExtendable2))
                        .propertyValue(ExtendableBean.P_PROVIDER_TYPE, typeof(ITestListenerRegistry2));
            }
        }

        public class DummyListener : ITestListener
        {
        }

        [LogInstance]
        public ILogger Log { private get; set; }

        [TestMethod]
        public void TestExtensionPoint()
        {
            InitManually(GetType());
            ITestListenerExtendable testListenerExtendable = BeanContext.GetService<ITestListenerExtendable>("testExtensionPoint");
            ITestListenerRegistry testListenerRegistry = BeanContext.GetService<ITestListenerRegistry>("testExtensionPoint");

            testListenerExtendable.AddTestListener(new DummyListener());

            ITestListener[] testListeners = testListenerRegistry.GetTestListeners();
            Assert.IsNotNull(testListeners);
            Assert.AreEqual(1, testListeners.Length);
        }

        [TestMethod]
        public void TestExtensionPointByType()
        {
            InitManually(GetType());
            ITestListenerExtendable2 testListenerExtendable = BeanContext.GetService<ITestListenerExtendable2>("testExtensionPoint2");
            ITestListenerRegistry2 testListenerRegistry = BeanContext.GetService<ITestListenerRegistry2>("testExtensionPoint2");

            testListenerExtendable.RegisterTestListener(new DummyListener(), typeof(IList));

            ITestListener testListener = testListenerRegistry.GetTestListener(typeof(ArrayList));
            Assert.IsNotNull(testListener);
        }
    }
}