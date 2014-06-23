using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Cache.Mock
{
    /**
     * Support for unit tests that do not include jAmbeth.Cache
     */
    public class CacheMockModule : IInitializingModule
    {
        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            beanContextFactory.RegisterBean("revertChangesHelper", typeof(RevertChangesHelperMock)).Autowireable(typeof(IRevertChangesHelper));
            beanContextFactory.RegisterBean("cache", typeof(CacheMock)).Autowireable(typeof(ICache));
            beanContextFactory.RegisterBean("cacheFactory", typeof(CacheFactoryMock)).Autowireable(typeof(ICacheFactory));
            beanContextFactory.RegisterBean("cacheProvider", typeof(CacheProviderMock)).Autowireable(typeof(ICacheProvider));
            beanContextFactory.RegisterBean("prefetchHelper", typeof(PrefetchHelperMock)).Autowireable(typeof(IPrefetchHelper));
        }
    }
}