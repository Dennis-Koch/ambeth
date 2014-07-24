using De.Osthus.Ambeth.Collections.Specialized;
using De.Osthus.Ambeth.Helloworld.Transfer;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Proxy;
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
        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [TestMethod]
        public void Test_Prefetch()
        {
            Object obj = new UsableObservableCollection<Object>();
            IList<TestEntity> testEntities = HelloWorldService.GetAllTestEntities();
            Assert.AssertNotEquals(0, testEntities.Count);

            IPrefetchHandle prefetch = PrefetchHelper.CreatePrefetch().Add(typeof(TestEntity), "Relation").Add(typeof(TestEntity), "Relations").Build();

            prefetch.Prefetch(testEntities);

            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(typeof(TestEntity));
            int indexOfRelation = metaData.GetIndexByRelationName("Relation");
            int indexOfRelations = metaData.GetIndexByRelationName("Relations");

            foreach (TestEntity testEntity in testEntities)
            {
                Assert.AssertTrue(((IObjRefContainer)testEntity).Is__Initialized(indexOfRelation));
                Assert.AssertTrue(((IObjRefContainer)testEntity).Is__Initialized(indexOfRelations));
            }
        }
    }
}