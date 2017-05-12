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
    [TestModule(typeof(BootstrapScannerModule), typeof(OriWrapperTestModule), typeof(XmlModule))]
    public class OriWrapperReadTest : AbstractInformationBusTest
    {
        [Autowired(XmlModule.CYCLIC_XML_HANDLER)]
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
        public static new void RegisterAssemblies(Microsoft.VisualStudio.TestTools.UnitTesting.TestContext context)
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
            Assert.AssertEquals(testData.obj, obj);
        }

        [TestMethod]
        public void ReadEntityWithRelation()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getEntityWithRelationTestData();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            Assert.AssertEquals(testData.obj, obj);
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
            De.Osthus.Ambeth.Transfer.Assert.AssertEquals((ServiceDescription)testData.obj, (ServiceDescription)obj);
        }

        [TestMethod]
        public void ReadCreatedEntity()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedEntityTestData();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            Assert.AssertTrue(obj is Material);
            AssertEquals((Material)testData.obj, (Material)obj);
        }

        [TestMethod]
        public void ReadCreatedEntityJavaXML()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedEntityTestData();
            Object obj = CyclicXmlHandler.Read(testData.xmlJava);
            Assert.AssertTrue(obj is Material);
            AssertEquals((Material)testData.obj, (Material)obj);
        }

        [TestMethod]
        public void ReadCreatedChildEntity()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedChildEntityTestData();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            Assert.AssertTrue(obj is Material);
            AssertEquals((Material)testData.obj, (Material)obj);
        }

        [TestMethod]
        public void ReadCreatedChildEntity2()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedChildEntityTestData2();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            Assert.AssertTrue(obj is Material);
            AssertEquals((Material)testData.obj, (Material)obj);
        }

        [TestMethod]
        public void ReadCreatedChildEntityJavaXML()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedChildEntityTestData();
            Object obj = CyclicXmlHandler.Read(testData.xmlJava);
            Assert.AssertTrue(obj is Material);
            AssertEquals((Material)testData.obj, (Material)obj);
        }

        [TestMethod]
        public void ReadCreatedParentAndChildEntities()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedParentAndChildEntitiesTestData();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            Assert.AssertTrue(obj is Material);
            AssertEquals((Material)testData.obj, (Material)obj);
        }

        [TestMethod]
        public void ReadCreatedParentAndChildEntities2()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedParentAndChildEntitiesTestData2();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            Assert.AssertTrue(obj is Material);
            AssertEquals((Material)testData.obj, (Material)obj);
        }

        [TestMethod]
        public void ReadCreatedParentAndChildEntitiesJavaXML()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedParentAndChildEntitiesTestData();
            Object obj = CyclicXmlHandler.Read(testData.xmlJava);
            Assert.AssertTrue(obj is Material);
            AssertEquals((Material)testData.obj, (Material)obj);
        }

        [TestMethod]
        public void ReadCreatedParentAndChildEntitiesInList()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedParentAndChildEntitiesInListTestData();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            Assert.AssertTrue(obj is IList);
            IList<Object> actuals = (IList<Object>)obj;
            AssertEquals((Material)((IList<Object>)testData.obj)[0], (Material)actuals[0]);
        }

        [TestMethod]
        public void ReadCreatedParentAndChildEntitiesInList2()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedParentAndChildEntitiesInListTestData2();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            Assert.AssertTrue(obj is IList);
            IList<Object> actuals = (IList<Object>)obj;
            AssertEquals((Material)((IList<Object>)testData.obj)[0], (Material)actuals[0]);
        }

        [TestMethod]
        public void ReadCreatedParentAndChildEntitiesInListJavaXML()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedParentAndChildEntitiesInListTestData();
            Object obj = CyclicXmlHandler.Read(testData.xmlJava);
            Assert.AssertTrue(obj is IList);
            IList<Object> actuals = (IList<Object>)obj;
            AssertEquals((Material)((IList<Object>)testData.obj)[0], (Material)actuals[0]);
        }

        [TestMethod]
        public void ReadMultipleCreatedEntities()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getMultipleCreatedEntitiesTestData();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            List<Object> expecteds = (List<Object>)testData.obj;
            List<Object> actuals = (List<Object>)obj;
            Assert.AssertEquals(expecteds.Count, actuals.Count);
            for (int i = 0; i < expecteds.Count; i++)
            {
                Material expected = expecteds[i] as Material;
                Material actual = actuals[i] as Material;
                AssertEquals(expected, actual);
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
            Assert.AssertEquals(expecteds.Count, actuals.Count);
            for (int i = 0; i < expecteds.Count; i++)
            {
                Material expected = expecteds[i] as Material;
                Material actual = actuals[i] as Material;
                AssertEquals(expected, actual);
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
            Assert.AssertEquals(expecteds.Count, actuals.Count);
            for (int i = 0; i < expecteds.Count; i++)
            {
                Material expected = expecteds[i] as Material;
                Material actual = actuals[i] as Material;
                AssertEquals(expected, actual);
            }
        }

        [TestMethod]
        public void ReadCreatedAndExistingChildren()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedAndExistingChildrenTestData();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            AssertEquals((EntityA)testData.obj, (EntityA)obj);
        }

        [TestMethod]
        public void ReadCreatedAndExistingChildren2()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedAndExistingChildrenTestData2();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            AssertEquals((EntityA)testData.obj, (EntityA)obj);
        }

        [TestMethod]
        public void ReadCreatedAndExistingChildrenJavaXML()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedAndExistingChildrenTestData();
            Object obj = CyclicXmlHandler.Read(testData.xmlJava);
            AssertEquals((EntityA)testData.obj, (EntityA)obj);
        }

        [TestMethod]
        public void ReadUpdatedEntity()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getUpdatedEntityTestData();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            Assert.AssertEquals(testData.obj, obj);
            AssertEquals((Material)testData.obj, (Material)obj);
        }

        [TestMethod]
        public void ReadUpdatedEntityJavaXML()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getUpdatedEntityTestData();
            Object obj = CyclicXmlHandler.Read(testData.xmlJava);
            Assert.AssertEquals(testData.obj, obj);
            AssertEquals((Material)testData.obj, (Material)obj);
        }

        [TestMethod]
        public void ReadCreatedAndUpdatedEntities()
        {
            InitManually(GetType());
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedAndUpdatedEntitiesTestData();
            Object obj = CyclicXmlHandler.Read(testData.xml);
            List<Object> expecteds = (List<Object>)testData.obj;
            List<Object> actuals = (List<Object>)obj;
            Assert.AssertEquals(expecteds.Count, actuals.Count);
            for (int i = 0; i < expecteds.Count; i++)
            {
                Material expected = expecteds[i] as Material;
                Material actual = actuals[i] as Material;
                AssertEquals(expected, actual);
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
            Assert.AssertEquals(expecteds.Count, actuals.Count);
            for (int i = 0; i < expecteds.Count; i++)
            {
                Material expected = expecteds[i] as Material;
                Material actual = actuals[i] as Material;
                AssertEquals(expected, actual);
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
            Assert.AssertEquals(expecteds.Count, actuals.Count);
            for (int i = 0; i < expecteds.Count; i++)
            {
                Material expected = expecteds[i] as Material;
                Material actual = actuals[i] as Material;
                AssertEquals(expected, actual);
            }
        }

        private void AreArraysEqual(object[] expected, object[] actual)
        {
            Assert.AssertEquals(expected.Length, actual.Length);
            for (int i = 0; i < expected.Length; i++)
            {
                Assert.AssertEquals(expected[i], actual[i]);
            }
        }

        protected void AreCollectionsEqual(ICollection expected, ICollection actual)
        {
            Assert.AssertEquals(expected.Count, actual.Count);
            IEnumerator expectedIter = expected.GetEnumerator();
            IEnumerator actualIter = actual.GetEnumerator();
            int index = 0;
            while (expectedIter.MoveNext())
            {
                Object expectedEntry = expectedIter.Current;
                actualIter.MoveNext();
                Object actualEntry = actualIter.Current;
                Assert.AssertEquals(expectedEntry, actualEntry, "collection first differend at element " + index++);
            }
        }

        protected void AreSetsEqual(ISet<Object> expected, ISet<Object> actual)
        {
            Assert.AssertEquals(expected.Count, actual.Count);
            IEnumerator expectedIter = expected.GetEnumerator();
            while (expectedIter.MoveNext())
            {
                Object expectedEntry = expectedIter.Current;
                Assert.AssertTrue(actual.Contains(expectedEntry), "expected does not contain element " + expectedEntry);
            }
        }

        protected void AssertEquals(Material expected, Material actual)
        {
            Assert.AssertEquals(expected.Buid, actual.Buid);
            Assert.AssertEquals(expected.CreatedBy, actual.CreatedBy);
            Assert.AssertEquals(expected.CreatedOn, actual.CreatedOn);
            Assert.AssertEquals(expected.Id, actual.Id);
            Assert.AssertEquals(expected.Name, actual.Name);
            Assert.AssertEquals(expected.UpdatedBy, actual.UpdatedBy);
            Assert.AssertEquals(expected.UpdatedOn, actual.UpdatedOn);
            Assert.AssertEquals(expected.Version, actual.Version);

            AssertEquals(expected.MaterialGroup, actual.MaterialGroup);
        }

        protected void AssertEquals(MaterialGroup expected, MaterialGroup actual)
        {
            if (expected == null)
            {
                Assert.AssertNull(actual);
                return;
            }

            Assert.AssertNotNull(actual);
            Assert.AssertEquals(expected.Buid, actual.Buid);
            Assert.AssertEquals(expected.Id, actual.Id);
            Assert.AssertEquals(expected.Name, actual.Name);
            Assert.AssertEquals(expected.Version, actual.Version);
        }

        private void AssertEquals(EntityA expected, EntityA actual)
        {
            if (expected == null)
            {
                Assert.AssertNull(actual);
                return;
            }

            Assert.AssertNotNull(actual);
            Assert.AssertEquals(expected.Id, actual.Id);
            Assert.AssertEquals(expected.Version, actual.Version);
            Assert.AssertEquals(expected.EntityBs.Count, actual.EntityBs.Count);
            for (int i = 0; i < expected.EntityBs.Count; i++)
            {
                AssertEquals(expected.EntityBs[i], actual.EntityBs[i]);
            }
        }

        private void AssertEquals(EntityB expected, EntityB actual)
        {
            if (expected == null)
            {
                Assert.AssertNull(actual);
                return;
            }

            Assert.AssertNotNull(actual);
            Assert.AssertEquals(expected.Id, actual.Id);
            Assert.AssertEquals(expected.Version, actual.Version);
        }
    }
}
