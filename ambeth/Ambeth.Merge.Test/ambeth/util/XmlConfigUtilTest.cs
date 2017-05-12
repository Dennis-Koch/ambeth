using System;
using System.Xml.Linq;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Testutil;
using De.Osthus.Ambeth.Util.Xml;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace De.Osthus.Ambeth.Util
{
    [TestClass]
    [TestModule(typeof(XmlConfigUtilTestModule))]
    public class XmlConfigUtilTest : AbstractIocTest
    {
        private const String AMBETH_FOLDER = "ambeth/";

        private const String AMBETH_SCHEMA_FOLDER = AMBETH_FOLDER + "schema/";

        private const String XSD_SIMPLE_TYPES_2_0 = AMBETH_SCHEMA_FOLDER + "ambeth_simple_types_2_0.xsd";

        private const String XSD_ORM_2_0 = AMBETH_SCHEMA_FOLDER + "ambeth_orm_2_0.xsd";

        private const String ORM_XML_SIMPLE = AMBETH_FOLDER + "util/orm_simple.xml";

        public class XmlConfigUtilTestModule : IInitializingModule
        {

            public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
            {
                beanContextFactory.RegisterBean<XmlConfigUtil>("xmlConfigUtil").Autowireable<IXmlConfigUtil>();
            }

        }

        [Autowired]
        protected IXmlConfigUtil xmlConfigUtil;

        [TestMethod]
        public void TestReadXmlFiles()
        {
            XDocument[] docs = xmlConfigUtil.ReadXmlFiles(ORM_XML_SIMPLE);
            Assert.AssertNotNull(docs);
            Assert.AssertEquals(1, docs.Length);
        }

        [TestMethod]
        public void TestCreateValidator()
        {
            IXmlValidator validator1 = xmlConfigUtil.CreateValidator(XSD_SIMPLE_TYPES_2_0);
            Assert.AssertNotNull(validator1);

            IXmlValidator validator2 = xmlConfigUtil.CreateValidator(XSD_SIMPLE_TYPES_2_0, XSD_ORM_2_0);
            Assert.AssertNotNull(validator2);

            XDocument doc = xmlConfigUtil.ReadXmlFiles(ORM_XML_SIMPLE)[0];
            validator2.Validate(doc);
        }
    }
}
