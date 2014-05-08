using De.Osthus.Ambeth.Cache;

namespace De.Osthus.Ambeth.Xml.Test
{
    public class CacheFactoryMock : ICacheFactory
    {
        public IDisposableCache Create(CacheFactoryDirective cacheFactoryDirective)
        {
            return new CacheMock();
        }

        public IDisposableCache Create(CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware, bool? useWeakEntries)
        {
            return new CacheMock();
        }
    }
}
