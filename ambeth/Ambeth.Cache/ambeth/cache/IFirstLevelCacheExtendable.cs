namespace De.Osthus.Ambeth.Cache
{
    public interface IFirstLevelCacheExtendable
    {
        void RegisterFirstLevelCache(IWritableCache firstLevelCache, CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware);

        void UnregisterFirstLevelCache(IWritableCache firstLevelCache, CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware);
    }
}