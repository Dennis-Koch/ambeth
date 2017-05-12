using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Testutil;
using De.Osthus.Ambeth.Transfer;
using De.Osthus.Ambeth.Util;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using De.Osthus.Ambeth.Test.Model;

namespace De.Osthus.Ambeth.Xml.Test
{
    public class XmlPostProcessTestModule : IInitializingModule
    {
        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            beanContextFactory.registerBean<TestXmlPostProcessor>("testXmlPostProcessor").autowireable<TestXmlPostProcessor>();
            beanContextFactory.link("testXmlPostProcessor").to<IXmlPostProcessorExtendable>().with("test1");
            beanContextFactory.link("testXmlPostProcessor").to<IXmlPostProcessorExtendable>().with("test2");
            beanContextFactory.link("testXmlPostProcessor").to<IXmlPostProcessorExtendable>().with("test3");
        }
    }
}
