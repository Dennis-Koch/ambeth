using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Test.Model;

namespace De.Osthus.Ambeth.Xml.Test
{
    public class OriWrapperTestModule : IInitializingModule
    {
        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            beanContextFactory.RegisterBean<OriWrapperTestBed>("oriWrapperTestBed").Autowireable(typeof(OriWrapperTestBed));

            IBeanConfiguration cacheRetrieverConf = beanContextFactory.RegisterAnonymousBean<CacheRetrieverMock>();
            beanContextFactory.Link(cacheRetrieverConf).To<ICacheRetrieverExtendable>().With(typeof(EntityA));
            beanContextFactory.Link(cacheRetrieverConf).To<ICacheRetrieverExtendable>().With(typeof(EntityB));
            beanContextFactory.Link(cacheRetrieverConf).To<ICacheRetrieverExtendable>().With(typeof(Material));
            beanContextFactory.Link(cacheRetrieverConf).To<ICacheRetrieverExtendable>().With(typeof(MaterialGroup));
        }
    }
}
