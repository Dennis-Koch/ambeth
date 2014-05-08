using System;
using System.Text;
using System.Collections.Generic;
using System.Linq;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using De.Osthus.Ambeth.Typeinfo;

namespace De.Osthus.Ambeth.Cache.Test
{
    [TestClass]
    public class RootCacheTest
    {
        [ClassInitialize]
        public static void SetUp(TestContext testContext)
        {
        }

        [ClassCleanup]
        public static void TearDown()
        {
        }

        [TestMethod]
        public void TestMethod1()
        {
            RootCache rootCache = new RootCache();

            uint id = 1;
            TestParentEntity parent = new TestParentEntity();
            parent.RecId = id++;
            parent.Version = 1;
            parent.property = "PropertyText";
            parent.fieldExtern = "FieldExternText";
            parent.SetFieldIntern("FieldInternText");

            IList<TestChildEntity> children = new List<TestChildEntity>();
            for (int a = 3; a-- > 0; )
            {
                TestChildEntity child = new TestChildEntity();
                child.RecId = id++;
                child.Version = 1;
                child.Parent = parent;

                children.Add(child);
            }
            parent.Children = children;

            rootCache.Put(parent);

            TestParentEntity result1 = (TestParentEntity)rootCache.GetObject(typeof(TestParentEntity), parent.RecId);

            Assert.IsInstanceOfType(result1, typeof(TestParentEntity));
            Assert.AreEqual(((TestParentEntity)result1).RecId, parent.RecId);
            Assert.AreEqual(((TestParentEntity)result1).Version, parent.Version);

            result1.Children[0].Property = "test0";

            TestParentEntity result2 = (TestParentEntity)rootCache.GetObject(typeof(TestParentEntity), parent.RecId);
            Assert.IsFalse(Object.ReferenceEquals(result1, result2), "RootCache does not clone results!");

            Assert.IsFalse(((IValueHolder)result2.Children).IsInitialized, "ValueHolder must be uninitialized at their beginning!");

            Assert.IsFalse(Object.Equals(result1.Children[0].Property, result2.Children[0].Property), "RootCache does not preserve root data state!");



            ChildCache childCache = new ChildCache();
            childCache.Parent = rootCache;

            TestParentEntity cacheResult1 = (TestParentEntity)childCache.GetObject(typeof(TestParentEntity), parent.RecId);
            TestParentEntity cacheResult2 = (TestParentEntity)childCache.GetObject(typeof(TestParentEntity), parent.RecId);
            Assert.IsTrue(Object.ReferenceEquals(cacheResult1, cacheResult2), "ChildCache does not return same results!");
        }
    }
}
