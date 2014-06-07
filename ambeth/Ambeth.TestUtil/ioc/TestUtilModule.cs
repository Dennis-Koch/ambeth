using System;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Merge;

namespace De.Osthus.Ambeth.Testutil
{
    public class TestUtilModule : IInitializingModule
    {
        private const String DUMMY_CACHE_SERVICE = "cacheService.dummy";

        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            IBeanConfiguration dummyServiceBC = beanContextFactory.RegisterBean<DummyService>(DUMMY_CACHE_SERVICE).Autowireable(typeof(IClientServiceFactory), typeof(ICacheRetriever));
            beanContextFactory.RegisterAlias(CacheModule.EXTERNAL_CACHE_SERVICE, DUMMY_CACHE_SERVICE);
            beanContextFactory.Link(dummyServiceBC).To<IMergeServiceExtensionExtendable>().With(typeof(Object));


            beanContextFactory.RegisterAlias(CacheModule.ROOT_CACHE_RETRIEVER, "cacheServiceRegistry");
        }
    }
}
