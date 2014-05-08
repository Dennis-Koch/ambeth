using System;
using System.Collections.Generic;
using System.Threading;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Ioc.Hierarchy;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Testutil;
using De.Osthus.Ambeth.Ioc.Exceptions;

namespace De.Osthus.Ambeth.Ioc.Link
{
    [TestClass]
    public class LinkContainerTest : AbstractIocTest
    {
        public const String REGISTRY_NAME = "testRegistryBeanName";

        public const String REGISTRY_PROPERTY_NAME = "TestListener";

        public const String REGISTRY_EVENT_PROPERTY_NAME = "PropertyChanged";

        public const String LISTENER_NAME = "testListenerBeanName";

        public const String LISTENER_DELEGATE_NAME = "testListenerDelegateBeanName";

        public const String ListenerProp = "listenerProp";

        public const String ListenerNameProp = "listenerNameProp";

        public const String RegistryProp = "registryProp";

        public const String OptionalProp = "optionalProp";

        public const String ExtendableTypeProp = "extendableTypeProp";

        public static int listenerReceivedCount;

        public static int propertyChangedReceivedCount;

        [LogInstance]
        public ILogger Log { private get; set; }

        protected IServiceContext childContext;

        protected ITestRegistry testRegistry;

        //[TestInitialize]
        public void InitManually()
        {
            base.InitManually(GetType());
            childContext = BeanContext.CreateService(typeof(LinkContainerTestModule));
            testRegistry = childContext.GetService<ITestRegistry>();
        }

        [TestCleanup]
        public void TearDown()
        {
            listenerReceivedCount = 0;
            propertyChangedReceivedCount = 0;
            if (childContext != null)
            {
                childContext.Dispose();
                childContext = null;
            }
        }

        protected void testValidContext(int expectedCount)
        {
            testValidContext(expectedCount, expectedCount, 0);
        }

        protected void testValidContext(int expectedCount, int expectedListenerReceivedCount, int expectedPropertyChangedReceivedCount)
        {
            InitManually();
            Assert.AreEqual(expectedCount, testRegistry.GetTestListeners().Length);
            Assert.AreEqual(expectedListenerReceivedCount, listenerReceivedCount);
            Assert.AreEqual(expectedPropertyChangedReceivedCount, propertyChangedReceivedCount);
        }

        protected void testValidContextEvent(int expectedCount)
        {
            InitManually();
            Assert.AreEqual(expectedCount, testRegistry.GetPceListeners().Length);
            Assert.AreEqual(expectedCount, propertyChangedReceivedCount);
        }

        [TestMethod]
        [ExpectedException(typeof(LinkException))]
        [TestProperties(Name = ListenerProp, Value = "BY_NAME")]
        [TestProperties(Name = RegistryProp, Value = "BY_EXTENDABLE")]
        [TestProperties(Name = ExtendableTypeProp, Value = "De.Osthus.Ambeth.Ioc.Link.ITestListenerExtendable2")]        
        public void test_NoRByType()
        {
            InitManually();
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_NAME")]
        [TestProperties(Name = RegistryProp, Value = "BY_EXTENDABLE")]
        [TestProperties(Name = ExtendableTypeProp, Value = "De.Osthus.Ambeth.Ioc.Link.ITestListenerExtendable2")]
        [TestProperties(Name = OptionalProp, Value = "true")]
        public void testOptional_NoRByType()
        {
            InitManually();
            // If no exception occurs everything is ok
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_NAME")]
        [TestProperties(Name = RegistryProp, Value = "BY_EXTENDABLE")]
        [TestProperties(Name = ListenerNameProp, Value = LISTENER_NAME + "_wrong_name")]
        [TestProperties(Name = OptionalProp, Value = "true")]
        public void testOptional_NoL()
        {
            InitManually();
            // If no exception occurs everything is ok
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_NAME")]
        [TestProperties(Name = RegistryProp, Value = "BY_EXTENDABLE")]
        public void testLByName_RByExtendable()
        {
            testValidContext(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_NAME")]
        [TestProperties(Name = RegistryProp, Value = "BY_NAME_AND_EXTENDABLE")]
        public void testLByName_RByNameAndExtendable()
        {
            testValidContext(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_NAME")]
        [TestProperties(Name = RegistryProp, Value = "BY_NAME_AND_EVENT")]
        public void testLByName_RByNameAndEvent()
        {
            testValidContext(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_NAME")]
        [TestProperties(Name = RegistryProp, Value = "BY_NAME_AND_PROPERTY")]
        public void testLByName_RByNameAndProperty()
        {
            testValidContext(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_NAME")]
        [TestProperties(Name = RegistryProp, Value = "BY_INSTANCE_AND_EXTENDABLE")]
        public void testLByName_RByInstanceAndExtendable()
        {
            testValidContext(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_NAME")]
        [TestProperties(Name = RegistryProp, Value = "BY_INSTANCE_AND_EVENT")]
        public void testLByName_RByInstanceAndEvent()
        {
            testValidContext(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_NAME")]
        [TestProperties(Name = RegistryProp, Value = "BY_INSTANCE_AND_PROPERTY")]
        public void testLByName_RByInstanceAndProperty()
        {
            testValidContext(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_NAME_AND_METHOD")]
        [TestProperties(Name = RegistryProp, Value = "BY_EXTENDABLE")]
        public void testLByNameAndMethod_RByExtendable()
        {
            testValidContext(1, 0, 1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_NAME_AND_METHOD")]
        [TestProperties(Name = RegistryProp, Value = "BY_NAME_AND_EXTENDABLE")]
        public void testLByNameAndMethod_RByNameAndExtendable()
        {
            testValidContext(1, 0, 1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_NAME_AND_METHOD")]
        [TestProperties(Name = RegistryProp, Value = "BY_NAME_AND_EVENT")]
        public void testLByNameAndMethod_RByNameAndEvent()
        {
            testValidContext(1, 0, 1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_NAME_AND_METHOD")]
        [TestProperties(Name = RegistryProp, Value = "BY_NAME_AND_PROPERTY")]
        public void testLByNameAndMethod_RByNameAndProperty()
        {
            testValidContext(1, 0, 1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_NAME_AND_METHOD")]
        [TestProperties(Name = RegistryProp, Value = "BY_INSTANCE_AND_EXTENDABLE")]
        public void testLByNameAndMethod_RByInstanceAndExtendable()
        {
            testValidContext(1, 0, 1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_NAME_AND_METHOD")]
        [TestProperties(Name = RegistryProp, Value = "BY_INSTANCE_AND_EVENT")]
        public void testLByNameAndMethod_RByInstanceAndEvent()
        {
            testValidContext(1, 0, 1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_NAME_AND_METHOD")]
        [TestProperties(Name = RegistryProp, Value = "BY_INSTANCE_AND_PROPERTY")]
        public void testLByNameAndMethod_RByInstanceAndProperty()
        {
            testValidContext(1, 0, 1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_CONF")]
        [TestProperties(Name = RegistryProp, Value = "BY_EXTENDABLE")]
        public void testLByConf_RByExtendable()
        {
            testValidContext(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_CONF")]
        [TestProperties(Name = RegistryProp, Value = "BY_NAME_AND_EXTENDABLE")]
        public void testLByConf_RByNameAndExtendable()
        {
            testValidContext(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_CONF")]
        [TestProperties(Name = RegistryProp, Value = "BY_NAME_AND_EVENT")]
        public void testLByConf_RByNameAndEvent()
        {
            testValidContext(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_CONF")]
        [TestProperties(Name = RegistryProp, Value = "BY_NAME_AND_PROPERTY")]
        public void testLByConf_RByNameAndProperty()
        {
            testValidContext(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_CONF")]
        [TestProperties(Name = RegistryProp, Value = "BY_INSTANCE_AND_EXTENDABLE")]
        public void testLByConf_RByInstanceAndExtendable()
        {
            testValidContext(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_CONF")]
        [TestProperties(Name = RegistryProp, Value = "BY_INSTANCE_AND_EVENT")]
        public void testLByConf_RByInstanceAndEvent()
        {
            testValidContext(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_CONF")]
        [TestProperties(Name = RegistryProp, Value = "BY_INSTANCE_AND_PROPERTY")]
        public void testLByConf_RByInstanceAndProperty()
        {
            testValidContext(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_INSTANCE")]
        [TestProperties(Name = RegistryProp, Value = "BY_EXTENDABLE")]
        public void testLByInstance_RByExtendable()
        {
            testValidContext(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_INSTANCE")]
        [TestProperties(Name = RegistryProp, Value = "BY_NAME_AND_EXTENDABLE")]
        public void testLByInstance_RByNameAndExtendable()
        {
            testValidContext(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_INSTANCE")]
        [TestProperties(Name = RegistryProp, Value = "BY_NAME_AND_EVENT")]
        public void testLByInstance_RByNameAndEvent()
        {
            testValidContext(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_INSTANCE")]
        [TestProperties(Name = RegistryProp, Value = "BY_NAME_AND_PROPERTY")]
        public void testLByInstance_RByNameAndProperty()
        {
            testValidContext(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_INSTANCE")]
        [TestProperties(Name = RegistryProp, Value = "BY_INSTANCE_AND_EXTENDABLE")]
        public void testLByInstance_RByInstance()
        {
            testValidContext(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_INSTANCE")]
        [TestProperties(Name = RegistryProp, Value = "BY_INSTANCE_AND_EVENT")]
        public void testLByInstance_RByInstanceAndEvent()
        {
            testValidContext(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_INSTANCE")]
        [TestProperties(Name = RegistryProp, Value = "BY_INSTANCE_AND_PROPERTY")]
        public void testLByInstance_RByInstanceAndProperty()
        {
            testValidContext(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_NAME_DELEGATE")]
        [TestProperties(Name = RegistryProp, Value = "BY_EXTENDABLE")]
        public void testLByNameDelegate_RByExtendable()
        {
            testValidContextEvent(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_NAME_DELEGATE")]
        [TestProperties(Name = RegistryProp, Value = "BY_NAME_AND_EXTENDABLE")]
        public void testLByNameDelegate_RByNameAndExtendable()
        {
            testValidContextEvent(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_NAME_DELEGATE")]
        [TestProperties(Name = RegistryProp, Value = "BY_NAME_AND_EVENT")]
        public void testLByNameDelegate_RByNameAndEvent()
        {
            testValidContextEvent(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_NAME_DELEGATE")]
        [TestProperties(Name = RegistryProp, Value = "BY_NAME_AND_PROPERTY")]
        public void testLByNameDelegate_RByNameAndProperty()
        {
            testValidContextEvent(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_NAME_DELEGATE")]
        [TestProperties(Name = RegistryProp, Value = "BY_INSTANCE_AND_EXTENDABLE")]
        public void testLByNameDelegate_RByInstanceAndExtendable()
        {
            testValidContextEvent(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_NAME_DELEGATE")]
        [TestProperties(Name = RegistryProp, Value = "BY_INSTANCE_AND_EVENT")]
        public void testLByNameDelegate_RByInstanceAndEvent()
        {
            testValidContextEvent(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_NAME_DELEGATE")]
        [TestProperties(Name = RegistryProp, Value = "BY_INSTANCE_AND_PROPERTY")]
        public void testLByNameDelegate_RByInstanceAndProperty()
        {
            testValidContextEvent(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_CONF_DELEGATE")]
        [TestProperties(Name = RegistryProp, Value = "BY_EXTENDABLE")]
        public void testLByConfDelegate_RByExtendable()
        {
            testValidContextEvent(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_CONF_DELEGATE")]
        [TestProperties(Name = RegistryProp, Value = "BY_NAME_AND_EXTENDABLE")]
        public void testLByConfDelegate_RByNameAndExtendable()
        {
            testValidContextEvent(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_CONF_DELEGATE")]
        [TestProperties(Name = RegistryProp, Value = "BY_NAME_AND_EVENT")]
        public void testLByConfDelegate_RByNameAndEvent()
        {
            testValidContextEvent(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_CONF_DELEGATE")]
        [TestProperties(Name = RegistryProp, Value = "BY_NAME_AND_PROPERTY")]
        public void testLByConfDelegate_RByNameAndProperty()
        {
            testValidContextEvent(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_CONF_DELEGATE")]
        [TestProperties(Name = RegistryProp, Value = "BY_INSTANCE_AND_EXTENDABLE")]
        public void testLByConfDelegate_RByInstanceAndExtendable()
        {
            testValidContextEvent(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_CONF_DELEGATE")]
        [TestProperties(Name = RegistryProp, Value = "BY_INSTANCE_AND_EVENT")]
        public void testLByConfDelegate_RByInstanceAndEvent()
        {
            testValidContextEvent(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_CONF_DELEGATE")]
        [TestProperties(Name = RegistryProp, Value = "BY_INSTANCE_AND_PROPERTY")]
        public void testLByConfDelegate_RByInstanceAndProperty()
        {
            testValidContextEvent(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_INSTANCE_DELEGATE")]
        [TestProperties(Name = RegistryProp, Value = "BY_EXTENDABLE")]
        public void testLByInstanceDelegate_RByExtendable()
        {
            testValidContextEvent(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_INSTANCE_DELEGATE")]
        [TestProperties(Name = RegistryProp, Value = "BY_NAME_AND_EXTENDABLE")]
        public void testLByInstanceDelegate_RByNameAndExtendable()
        {
            testValidContextEvent(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_INSTANCE_DELEGATE")]
        [TestProperties(Name = RegistryProp, Value = "BY_NAME_AND_EVENT")]
        public void testLByInstanceDelegate_RByNameAndEvent()
        {
            testValidContextEvent(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_INSTANCE_DELEGATE")]
        [TestProperties(Name = RegistryProp, Value = "BY_NAME_AND_PROPERTY")]
        public void testLByInstanceDelegate_RByNameAndProperty()
        {
            testValidContextEvent(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_INSTANCE_DELEGATE")]
        [TestProperties(Name = RegistryProp, Value = "BY_INSTANCE_AND_EXTENDABLE")]
        public void testLByInstanceDelegate_RByInstance()
        {
            testValidContextEvent(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_INSTANCE_DELEGATE")]
        [TestProperties(Name = RegistryProp, Value = "BY_INSTANCE_AND_EVENT")]
        public void testLByInstanceDelegate_RByInstanceAndEvent()
        {
            testValidContextEvent(1);
        }

        [TestMethod]
        [TestProperties(Name = ListenerProp, Value = "BY_INSTANCE_DELEGATE")]
        [TestProperties(Name = RegistryProp, Value = "BY_INSTANCE_AND_PROPERTY")]
        public void testLByInstanceDelegate_RByInstanceAndProperty()
        {
            testValidContextEvent(1);
        }
    }
}