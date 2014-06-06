using System;
using System.Collections;
using System.Collections.Generic;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Test.Model;
using De.Osthus.Ambeth.Testutil;
using De.Osthus.Ambeth.Transfer;
using De.Osthus.Ambeth.Util;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace De.Osthus.Ambeth.Xml.Test
{
    [TestClass]
    [TestProperties(File = "ambeth/xml/oriwrapper/OriWrapperTestData.properties")]
    [TestProperties(Name = ServiceConfigurationConstants.MappingFile, Value = "ambeth/xml/oriwrapper/orm.xml")]
    [TestModule(typeof(OriWrapperTestModule), typeof(XmlModule))]
    public class OriWrapperReadTest : AbstractIndependentClientTest
    {
        [Autowired]
        public ICyclicXmlHandler CyclicXmlHandler { protected get; set; }

        [Autowired]
        public OriWrapperTestBed OriWrapperTestBed { protected get; set; }

        public override void AfterPropertiesSet()
        {
            base.AfterPropertiesSet();

            OriWrapperTestBed.Init();
        }

        /// <summary>
        /// Workaround to get all needed assemblies known by the AssemblyHelper.
        /// </summary>
        /// <param name="context"></param>
        [AssemblyInitialize]
        public static new void RegisterAssemblies(TestContext context)
        {
            AbstractIocTest.RegisterAssemblies(context);

            AssemblyHelper.RegisterAssemblyFromType(typeof(Material));
        }

        [TestMethod]
        public void ReadSimpleEntity()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getSimpleEntityTestData();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            Assert.AreEqual(testData.obj, obj);
        }

        [TestMethod]
        public void ReadEntityWithRelation()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getEntityWithRelationTestData();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            Assert.AreEqual(testData.obj, obj);
        }

        [TestMethod]
        public void ReadMixedArray()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getMixedArrayTestData();
            object obj = CyclicXmlHandler.Read(testData.xml);
            AreArraysEqual((object[])testData.obj, (object[])obj);
        }

        [TestMethod]
        public void ReadMixedList()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getMixedListTestData();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            AreCollectionsEqual((ICollection)testData.obj, (ICollection)obj);
        }

        [TestMethod]
        public void ReadMixedLinkedSet()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getMixedLinkedSetTestData();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            AreSetsEqual((ISet<Object>)testData.obj, (ISet<Object>)obj);
        }

        [TestMethod]
        public void ReadServiceDescription()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getServiceDescriptionTestData();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            De.Osthus.Ambeth.Transfer.Test.Assert.AreEqual((ServiceDescription)testData.obj, (ServiceDescription)obj);
        }

        [TestMethod]
        public void ReadCreatedEntity()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedEntityTestData();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            Assert.IsTrue(obj is Material);
            AreEqual((Material)testData.obj, (Material)obj);
        }

        [TestMethod]
        public void ReadCreatedEntityJavaXML()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedEntityTestData();
            Object obj = CyclicXmlHandler.Read(testData.xmlJava);
            Assert.IsTrue(obj is Material);
            AreEqual((Material)testData.obj, (Material)obj);
        }

        [TestMethod]
        public void ReadCreatedChildEntity()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedChildEntityTestData();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            Assert.IsTrue(obj is Material);
            AreEqual((Material)testData.obj, (Material)obj);
        }

        [TestMethod]
        public void ReadCreatedChildEntity2()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedChildEntityTestData2();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            Assert.IsTrue(obj is Material);
            AreEqual((Material)testData.obj, (Material)obj);
        }

        [TestMethod]
        public void ReadCreatedChildEntityJavaXML()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedChildEntityTestData();
            Object obj = CyclicXmlHandler.Read(testData.xmlJava);
            Assert.IsTrue(obj is Material);
            AreEqual((Material)testData.obj, (Material)obj);
        }

        [TestMethod]
        public void ReadCreatedParentAndChildEntities()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedParentAndChildEntitiesTestData();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            Assert.IsTrue(obj is Material);
            AreEqual((Material)testData.obj, (Material)obj);
        }

        [TestMethod]
        public void ReadCreatedParentAndChildEntities2()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedParentAndChildEntitiesTestData2();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            Assert.IsTrue(obj is Material);
            AreEqual((Material)testData.obj, (Material)obj);
        }

        [TestMethod]
        public void ReadCreatedParentAndChildEntitiesJavaXML()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedParentAndChildEntitiesTestData();
            Object obj = CyclicXmlHandler.Read(testData.xmlJava);
            Assert.IsTrue(obj is Material);
            AreEqual((Material)testData.obj, (Material)obj);
        }

        [TestMethod]
        public void ReadCreatedParentAndChildEntitiesInList()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedParentAndChildEntitiesInListTestData();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            Assert.IsTrue(obj is IList);
            IList<Object> actuals = (IList<Object>)obj;
            AreEqual((Material)((IList<Object>)testData.obj)[0], (Material)actuals[0]);
        }

        [TestMethod]
        public void ReadCreatedParentAndChildEntitiesInList2()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedParentAndChildEntitiesInListTestData2();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            Assert.IsTrue(obj is IList);
            IList<Object> actuals = (IList<Object>)obj;
            AreEqual((Material)((IList<Object>)testData.obj)[0], (Material)actuals[0]);
        }

        [TestMethod]
        public void ReadCreatedParentAndChildEntitiesInListJavaXML()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedParentAndChildEntitiesInListTestData();
            Object obj = CyclicXmlHandler.Read(testData.xmlJava);
            Assert.IsTrue(obj is IList);
            IList<Object> actuals = (IList<Object>)obj;
            AreEqual((Material)((IList<Object>)testData.obj)[0], (Material)actuals[0]);
        }

        [TestMethod]
        public void ReadMultipleCreatedEntities()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getMultipleCreatedEntitiesTestData();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            List<Object> expecteds = (List<Object>)testData.obj;
            List<Object> actuals = (List<Object>)obj;
            Assert.AreEqual(expecteds.Count, actuals.Count);
            for (int i = 0; i < expecteds.Count; i++)
            {
                Material expected = expecteds[i] as Material;
                Material actual = actuals[i] as Material;
                AreEqual(expected, actual);
            }
        }

        [TestMethod]
        public void ReadMultipleCreatedEntities2()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getMultipleCreatedEntitiesTestData2();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            List<Object> expecteds = (List<Object>)testData.obj;
            List<Object> actuals = (List<Object>)obj;
            Assert.AreEqual(expecteds.Count, actuals.Count);
            for (int i = 0; i < expecteds.Count; i++)
            {
                Material expected = expecteds[i] as Material;
                Material actual = actuals[i] as Material;
                AreEqual(expected, actual);
            }
        }

        [TestMethod]
        public void ReadMultipleCreatedEntitiesJavaXML()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getMultipleCreatedEntitiesTestData();
            Object obj = CyclicXmlHandler.Read(testData.xmlJava);
            List<Object> expecteds = (List<Object>)testData.obj;
            List<Object> actuals = (List<Object>)obj;
            Assert.AreEqual(expecteds.Count, actuals.Count);
            for (int i = 0; i < expecteds.Count; i++)
            {
                Material expected = expecteds[i] as Material;
                Material actual = actuals[i] as Material;
                AreEqual(expected, actual);
            }
        }

        [TestMethod]
        public void ReadCreatedAndExistingChildren()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedAndExistingChildrenTestData();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            AreEqual((EntityA)testData.obj, (EntityA)obj);
        }

        [TestMethod]
        public void ReadCreatedAndExistingChildren2()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedAndExistingChildrenTestData2();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            AreEqual((EntityA)testData.obj, (EntityA)obj);
        }

        [TestMethod]
        public void ReadCreatedAndExistingChildrenJavaXML()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedAndExistingChildrenTestData();
            Object obj = CyclicXmlHandler.Read(testData.xmlJava);
            AreEqual((EntityA)testData.obj, (EntityA)obj);
        }

        [TestMethod]
        public void ReadUpdatedEntity()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getUpdatedEntityTestData();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            Assert.AreEqual(testData.obj, obj);
            AreEqual((Material)testData.obj, (Material)obj);
        }

        [TestMethod]
        public void ReadUpdatedEntityJavaXML()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getUpdatedEntityTestData();
            Object obj = CyclicXmlHandler.Read(testData.xmlJava);
            Assert.AreEqual(testData.obj, obj);
            AreEqual((Material)testData.obj, (Material)obj);
        }

        [TestMethod]
        public void ReadCreatedAndUpdatedEntities()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedAndUpdatedEntitiesTestData();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            List<Object> expecteds = (List<Object>)testData.obj;
            List<Object> actuals = (List<Object>)obj;
            Assert.AreEqual(expecteds.Count, actuals.Count);
            for (int i = 0; i < expecteds.Count; i++)
            {
                Material expected = expecteds[i] as Material;
                Material actual = actuals[i] as Material;
                AreEqual(expected, actual);
            }
        }

        [TestMethod]
        public void ReadCreatedAndUpdatedEntities2()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedAndUpdatedEntitiesTestData2();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            List<Object> expecteds = (List<Object>)testData.obj;
            List<Object> actuals = (List<Object>)obj;
            Assert.AreEqual(expecteds.Count, actuals.Count);
            for (int i = 0; i < expecteds.Count; i++)
            {
                Material expected = expecteds[i] as Material;
                Material actual = actuals[i] as Material;
                AreEqual(expected, actual);
            }
        }

        [TestMethod]
        public void ReadCreatedAndUpdatedEntitiesJavaXML()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedAndUpdatedEntitiesTestData();
            Object obj = CyclicXmlHandler.Read(testData.xmlJava);
            List<Object> expecteds = (List<Object>)testData.obj;
            List<Object> actuals = (List<Object>)obj;
            Assert.AreEqual(expecteds.Count, actuals.Count);
            for (int i = 0; i < expecteds.Count; i++)
            {
                Material expected = expecteds[i] as Material;
                Material actual = actuals[i] as Material;
                AreEqual(expected, actual);
            }
        }

        private void AreArraysEqual(object[] expected, object[] actual)
        {
            Assert.AreEqual(expected.Length, actual.Length);
            for (int i = 0; i < expected.Length; i++)
            {
                Assert.AreEqual(expected[i], actual[i]);
            }
        }

        protected void AreCollectionsEqual(ICollection expected, ICollection actual)
        {
            Assert.AreEqual(expected.Count, actual.Count);
            IEnumerator expectedIter = expected.GetEnumerator();
            IEnumerator actualIter = actual.GetEnumerator();
            int index = 0;
            while (expectedIter.MoveNext())
            {
                Object expectedEntry = expectedIter.Current;
                actualIter.MoveNext();
                Object actualEntry = actualIter.Current;
                Assert.AreEqual(expectedEntry, actualEntry, "collection first differend at element " + index++);
            }
        }

        protected void AreSetsEqual(ISet<Object> expected, ISet<Object> actual)
        {
            Assert.AreEqual(expected.Count, actual.Count);
            IEnumerator expectedIter = expected.GetEnumerator();
            while (expectedIter.MoveNext())
            {
                Object expectedEntry = expectedIter.Current;
                Assert.IsTrue(actual.Contains(expectedEntry), "expected does not contain element " + expectedEntry);
            }
        }

        protected void AreEqual(Material expected, Material actual)
        {
            Assert.AreEqual(expected.Buid, actual.Buid);
            Assert.AreEqual(expected.CreatedBy, actual.CreatedBy);
            Assert.AreEqual(expected.CreatedOn, actual.CreatedOn);
            Assert.AreEqual(expected.Id, actual.Id);
            Assert.AreEqual(expected.Name, actual.Name);
            Assert.AreEqual(expected.UpdatedBy, actual.UpdatedBy);
            Assert.AreEqual(expected.UpdatedOn, actual.UpdatedOn);
            Assert.AreEqual(expected.Version, actual.Version);

            AreEqual(expected.MaterialGroup, actual.MaterialGroup);
        }

        protected void AreEqual(MaterialGroup expected, MaterialGroup actual)
        {
            if (expected == null)
            {
                Assert.IsNull(actual);
                return;
            }

            Assert.IsNotNull(actual);
            Assert.AreEqual(expected.Buid, actual.Buid);
            Assert.AreEqual(expected.Id, actual.Id);
            Assert.AreEqual(expected.Name, actual.Name);
            Assert.AreEqual(expected.Version, actual.Version);
        }

        private void AreEqual(EntityA expected, EntityA actual)
        {
            if (expected == null)
            {
                Assert.IsNull(actual);
                return;
            }

            Assert.IsNotNull(actual);
            Assert.AreEqual(expected.Id, actual.Id);
            Assert.AreEqual(expected.Version, actual.Version);
            Assert.AreEqual(expected.EntityBs.Count, actual.EntityBs.Count);
            for (int i = 0; i < expected.EntityBs.Count; i++)
            {
                AreEqual(expected.EntityBs[i], actual.EntityBs[i]);
            }
        }

        private void AreEqual(EntityB expected, EntityB actual)
        {
            if (expected == null)
            {
                Assert.IsNull(actual);
                return;
            }

            Assert.IsNotNull(actual);
            Assert.AreEqual(expected.Id, actual.Id);
            Assert.AreEqual(expected.Version, actual.Version);
        }
    }
}
