using System;
namespace De.Osthus.Ambeth.Cache.Mock
{
    /**
     * Support for unit tests that do not include jAmbeth.Cache
     */
    public class CacheFactoryMock : ICacheFactory
    {
        public IDisposableCache Create(CacheFactoryDirective cacheFactoryDirective, String name)
        {
            return null;
        }

        public IDisposableCache Create(CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware, bool? useWeakEntries, String name)
        {
            return null;
        }

        public IDisposableCache CreatePrivileged(CacheFactoryDirective cacheFactoryDirective, String name)
        {
            return null;
        }

        public IDisposableCache CreatePrivileged(CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware, bool? useWeakEntries, String name)
        {
            return null;
        }
    }
}