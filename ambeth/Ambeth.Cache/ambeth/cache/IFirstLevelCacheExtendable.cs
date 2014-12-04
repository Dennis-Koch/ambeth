using System;

namespace De.Osthus.Ambeth.Cache
{
    public interface IFirstLevelCacheExtendable
    {
        void RegisterFirstLevelCache(IWritableCache firstLevelCache, CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware);

        void UnregisterFirstLevelCache(IWritableCache firstLevelCache, CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware);

        void RegisterFirstLevelCache(IWritableCache firstLevelCache, CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware, String name);

        void UnregisterFirstLevelCache(IWritableCache firstLevelCache, CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware, String name);
    }
}