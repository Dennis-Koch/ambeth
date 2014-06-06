using System;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Testutil
{
    public class TestUtilModule : IInitializingModule
    {
        private const String DUMMY_CACHE_SERVICE = "cacheService.dummy";

        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            beanContextFactory.RegisterBean<DummyService>(DUMMY_CACHE_SERVICE).Autowireable(typeof(IClientServiceFactory), typeof(IMergeService), typeof(ICacheRetriever));
            beanContextFactory.RegisterAlias(CacheModule.EXTERNAL_CACHE_SERVICE, DUMMY_CACHE_SERVICE);

            beanContextFactory.RegisterAlias(CacheModule.ROOT_CACHE_RETRIEVER, "cacheServiceRegistry");
        }
    }
}
