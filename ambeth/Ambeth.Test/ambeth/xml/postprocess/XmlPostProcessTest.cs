using System;
using System.Collections;
using System.Collections.Generic;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Testutil;
using De.Osthus.Ambeth.Transfer;
using De.Osthus.Ambeth.Util;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using De.Osthus.Ambeth.Test.Model;

namespace De.Osthus.Ambeth.Xml.Test
{
    [TestClass]
    [TestFrameworkModule(typeof(CacheMockTestModule))]
    [TestModule(typeof(XmlBootstrapModule), typeof(XmlBootstrapTestModule), typeof(OriWrapperTestModule), typeof(XmlPostProcessTestModule))]
    public class XmlPostProcessTest : AbstractIocTest
    {
        [Autowired]
        public ICyclicXMLHandler CyclicXmlHandler { protected get; set; }

        [Autowired]
        public TestXmlPostProcessor TestXmlPostProcessor { protected get; set; }
        
        [TestMethod]
        public void testPostProcessTag()
        {
            String xml = "<root><n/><pp><test1/><test2/><test3/></pp></root>";
            cyclicXmlHandler.read(xml);
            Assert.AreEqual(3, testXmlPostProcessor.handledTags.size());
            Assert.AreEqual("test1", testXmlPostProcessor.handledTags[0]);
            Assert.AreEqual("test2", testXmlPostProcessor.handledTags[1]);
            Assert.AreEqual("test3", testXmlPostProcessor.handledTags[2]);
        }

        [TestMethod]
        public void testPostProcessTag_exception()
        {
            String xml = "<root><n/><pp><test1/><test2/><test4/></pp></root>";
            try
            {
                cyclicXmlHandler.Read(xml);
            }
            catch (MaskingRuntimeException e)
            {
                return;
            }
            Assert.Fail();
        }
    }
}
