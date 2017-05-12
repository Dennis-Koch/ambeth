using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Merge.Config;
using De.Osthus.Ambeth.Merge.Independent;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Util.Xml;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using De.Osthus.Ambeth.Testutil;

namespace De.Osthus.Ambeth.Merge.Independent
{
    [TestClass]
    [TestProperties(Name = ServiceConfigurationConstants.MappingFile, Value = rootPath + "independent-orm.xml;" + rootPath + "independent-orm2.xml")]
    [TestProperties(Name = ServiceConfigurationConstants.ValueObjectFile, Value = rootPath + "independent-vo-config.xml;" + rootPath + "independent-vo-config2.xml")]
    public class IndependentEntityMetaDataClient20Test : IndependentEntityMetaDataClientTest
    {
        [TestMethod]
        public override void TestGetMetaData()
        {
            base.TestGetMetaData();
        }

        [TestMethod]
        [ExpectedException(typeof(ArgumentException))]
        public override void TestGetMetaData_Exception()
        {
            base.TestGetMetaData_Exception();
        }

        [TestMethod]
        public override void TestGetMetaData_TryOnly()
        {
            base.TestGetMetaData_TryOnly();
        }

        [TestMethod]
        [ExpectedException(typeof(ArgumentException))]
        public override void TestGetMetaData_TryOnlyWithException()
        {
            base.TestGetMetaData_TryOnlyWithException();
        }

        [TestMethod]
        public override void TestGetMetaData_List()
        {
            base.TestGetMetaData_List();
        }

        [TestMethod]
        public override void TestMetaDataContent_EntityA()
        {
            base.TestMetaDataContent_EntityA();
        }

        [Ignore]
        [TestMethod]
        public override void TestRegisterValueObjectConfig()
        {
            base.TestRegisterValueObjectConfig();
        }

        [Ignore]
        [TestMethod]
        public override void TestUnregisterValueObjectConfig()
        {
            base.TestUnregisterValueObjectConfig();
        }

        [TestMethod]
        public override void TestGetValueObjectConfig()
        {
            base.TestGetValueObjectConfig();
        }

        [Ignore]
        [TestMethod]
        public override void TestRegisterEntityMetaData()
        {
            base.TestRegisterEntityMetaData();
        }

        [Ignore]
        [TestMethod]
        public override void TestUnregisterEntityMetaData()
        {
            base.TestUnregisterEntityMetaData();
        }

        [TestMethod]
        public override void TestEmbeddedTypeAccess()
        {
            base.TestEmbeddedTypeAccess();
        }
    }
}
