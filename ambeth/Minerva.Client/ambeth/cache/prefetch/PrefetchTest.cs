using De.Osthus.Ambeth.Collections.Specialized;
using De.Osthus.Ambeth.Helloworld.Transfer;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Test;
using De.Osthus.Ambeth.Testutil;
using De.Osthus.Ambeth.Util;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Cache.Prefetch
{
    [TestRebuildContext]
    [TestClass]
    public class PrefetchTest : AbstractHelloWorldTest
    {
        [TestMethod]
        public void test_Prefetch()
        {
            Object obj = new UsableObservableCollection<Object>();
            IList<TestEntity> testEntities = HelloWorldService.GetAllTestEntities();
            Assert.AreNotEqual(0, testEntities.Count);

            IPrefetchHandle prefetch = PrefetchHelper.CreatePrefetch().Add(typeof(TestEntity), "Relation").Add(typeof(TestEntity), "Relations").Build();

            prefetch.Prefetch(testEntities);

            foreach (TestEntity testEntity in testEntities)
            {
                Assert.AreEqual(true, ProxyHelper.IsInitialized(testEntity, "Relation"));
                Assert.AreEqual(true, ProxyHelper.IsInitialized(testEntity, "Relations"));
            }
        }
    }
}