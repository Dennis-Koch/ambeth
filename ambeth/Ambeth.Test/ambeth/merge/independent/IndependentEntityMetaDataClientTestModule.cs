using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Util.Xml;
using De.Osthus.Ambeth.Xml;

namespace De.Osthus.Ambeth.Merge.Independent
{
    public class IndependentEntityMetaDataClientTestModule : IInitializingModule
    {
        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            beanContextFactory.RegisterBean<XmlConfigUtil>("xmlConfigUtil").Autowireable(typeof(IXmlConfigUtil));
            beanContextFactory.RegisterBean<XmlTypeHelper>("xmlTypeHelper").Autowireable<IXmlTypeHelper>();
        }
    }
}
