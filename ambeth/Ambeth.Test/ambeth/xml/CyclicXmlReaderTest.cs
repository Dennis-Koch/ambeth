using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Testutil;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Xml
{
    [TestClass]
    [TestModule(typeof(BootstrapScannerModule), typeof(XmlModule))]
    public class CyclicXMLReaderTest : AbstractInformationBusTest
    {
        [Autowired(XmlModule.CYCLIC_XML_HANDLER)]
        public ICyclicXmlHandler CyclicXmlHandler { protected get; set; }

        [TestMethod]
        public void CyclicTestReadMetaData()
        {
            String xml = "<root><l i=\"1\" s=\"2\" ti=\"2\" n=\"Object\"><o i=\"3\" ti=\"4\" n=\"EntityMetaDataTransfer\" m=\"AlternateIdMemberIndicesInPrimitives AlternateIdMemberNames CreatedByMemberName CreatedOnMemberName EntityType IdMemberName MergeRelevantNames PrimitiveMemberNames RelationMemberNames TypesRelatingToThis TypesToCascadeDelete UpdatedByMemberName UpdatedOnMemberName VersionMemberName\"><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/></o><r i=\"1\"/></l></root>";
            List<Object> list = (List<Object>)CyclicXmlHandler.Read(xml);
            Assert.AssertNotNull(list);
            Assert.AssertEquals(2, list.Count);
        }
    }
}
