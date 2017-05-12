using System;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Testutil;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace De.Osthus.Ambeth.Xml.Test
{
    [TestClass]
    [TestProperties(File = "ambeth/xml/oriwrapper/OriWrapperTestData.properties")]
    [TestProperties(Name = ServiceConfigurationConstants.MappingFile, Value = "ambeth/xml/oriwrapper/orm.xml")]
    [TestModule(typeof(BootstrapScannerModule), typeof(OriWrapperTestModule), typeof(XmlModule))]
    public class OriWrapperWriteTest : AbstractInformationBusTest
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

        [TestMethod]
        public void WriteSimpleEntity()
        {
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getSimpleEntityTestData();
            String xml = CyclicXmlHandler.Write(testData.obj);
            Assert.AssertEquals(testData.xml, xml);
        }

        [TestMethod]
        public void WriteEntityWithRelation()
        {
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getEntityWithRelationTestData();
            String xml = CyclicXmlHandler.Write(testData.obj);
            Assert.AssertEquals(testData.xml, xml);
        }

        [TestMethod]
        public void WriteMixedArray()
        {
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getMixedArrayTestData();
            String xml = CyclicXmlHandler.Write(testData.obj);
            Assert.AssertEquals(testData.xml, xml);
        }

        [TestMethod]
        public void WriteMixedList()
        {
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getMixedListTestData();
            String xml = CyclicXmlHandler.Write(testData.obj);
            Assert.AssertEquals(testData.xml, xml);
        }

        [TestMethod]
        public void WriteMixedLinkedSet()
        {
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getMixedLinkedSetTestData();
            String xml = CyclicXmlHandler.Write(testData.obj);
            Assert.AssertEquals(testData.xml, xml);
        }

        [TestMethod]
        public void WriteServiceDescription()
        {
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getServiceDescriptionTestData();
            String xml = CyclicXmlHandler.Write(testData.obj);
            Assert.AssertEquals(testData.xml, xml);
        }

        [TestMethod]
        public void WriteCreatedEntity()
        {
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedEntityTestData();
            String xml = CyclicXmlHandler.Write(testData.obj);
            Assert.AssertEquals(testData.xml, xml);
        }

        [TestMethod]
        public void WriteCreatedChildEntity()
        {
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedChildEntityTestData();
            String xml = CyclicXmlHandler.Write(testData.obj);
            if (!testData.xml.Equals(xml))
            {
                // Compensate for loss of order in set in CUDResut
                testData = OriWrapperTestBed.getCreatedChildEntityTestData2();
            }
            Assert.AssertEquals(testData.xml, xml);
        }

        [TestMethod]
        public void WriteCreatedParentAndChildEntities()
        {
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedParentAndChildEntitiesTestData();
            String xml = CyclicXmlHandler.Write(testData.obj);
            if (!testData.xml.Equals(xml))
            {
                // Compensate for loss of order in set
                testData = OriWrapperTestBed.getCreatedParentAndChildEntitiesTestData2();
            }
            Assert.AssertEquals(testData.xml, xml);
        }

        [TestMethod]
        public void WriteCreatedParentAndChildEntitiesInList()
        {
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedParentAndChildEntitiesInListTestData();
            String xml = CyclicXmlHandler.Write(testData.obj);
            if (!testData.xml.Equals(xml))
            {
                // Compensate for loss of order in set
                testData = OriWrapperTestBed.getCreatedParentAndChildEntitiesInListTestData2();
            }
            Assert.AssertEquals(testData.xml, xml);
        }

        [TestMethod]
        public void WriteMultipleCreatedEntities()
        {
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getMultipleCreatedEntitiesTestData();
            String xml = CyclicXmlHandler.Write(testData.obj);
            if (!testData.xml.Equals(xml))
            {
                // Compensate for loss of order in set in CUDResut
                testData = OriWrapperTestBed.getMultipleCreatedEntitiesTestData2();
            }
            Assert.AssertEquals(testData.xml, xml);
        }

        [TestMethod]
        public void WriteUpdatedEntity()
        {
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getUpdatedEntityTestData();
            String xml = CyclicXmlHandler.Write(testData.obj);
            Assert.AssertEquals(testData.xml, xml);
        }

        [TestMethod]
        public void WriteCreatedAndUpdatedEntities()
        {
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedAndUpdatedEntitiesTestData();
            String xml = CyclicXmlHandler.Write(testData.obj);
            if (!testData.xml.Equals(xml))
            {
                // Compensate for loss of order in set in CUDResut
                testData = OriWrapperTestBed.getCreatedAndUpdatedEntitiesTestData2();
            }
            Assert.AssertEquals(testData.xml, xml);
        }

        [TestMethod]
        public void WriteCreatedAndExistingChildren()
        {
            OriWrapperTestBed.TestData testData = OriWrapperTestBed.getCreatedAndExistingChildrenTestData();
            String xml = CyclicXmlHandler.Write(testData.obj);
            if (!testData.xml.Equals(xml))
            {
                // Compensate for loss of order in set in CUDResut
                testData = OriWrapperTestBed.getCreatedAndExistingChildrenTestData2();
            }
            Assert.AssertEquals(testData.xml, xml);
        }
    }
}
