package de.osthus.ambeth.cache;

public interface IFirstLevelCacheExtendable
{
	void registerFirstLevelCache(IWritableCache firstLevelCache, CacheFactoryDirective cacheFactoryDirective, boolean foreignThreadAware);

	void unregisterFirstLevelCache(IWritableCache firstLevelCache, CacheFactoryDirective cacheFactoryDirective, boolean foreignThreadAware);

	void registerFirstLevelCache(IWritableCache firstLevelCache, CacheFactoryDirective cacheFactoryDirective, boolean foreignThreadAware, String name);

	void unregisterFirstLevelCache(IWritableCache firstLevelCache, CacheFactoryDirective cacheFactoryDirective, boolean foreignThreadAware, String name);
}