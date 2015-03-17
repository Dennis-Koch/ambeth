using De.Osthus.Ambeth.Cache;
using System;

namespace De.Osthus.Ambeth.Xml.Test
{
    public class CacheFactoryMock : ICacheFactory
    {
        public IDisposableCache Create(CacheFactoryDirective cacheFactoryDirective, String name)
        {
            return new CacheMock();
        }

        public IDisposableCache Create(CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware, bool? useWeakEntries, String name)
        {
            return new CacheMock();
        }

        public IDisposableCache CreatePrivileged(CacheFactoryDirective cacheFactoryDirective, String name)
        {
            return new CacheMock();
        }

        public IDisposableCache CreatePrivileged(CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware, bool? useWeakEntries, String name)
        {
            return new CacheMock();
        }
    }
}
