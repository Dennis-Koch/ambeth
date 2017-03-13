package com.koch.ambeth.cache;

import com.koch.ambeth.merge.cache.CacheFactoryDirective;
import com.koch.ambeth.merge.cache.IWritableCache;

public interface IFirstLevelCacheExtendable
{
	void registerFirstLevelCache(IWritableCache firstLevelCache, CacheFactoryDirective cacheFactoryDirective, boolean foreignThreadAware);

	void unregisterFirstLevelCache(IWritableCache firstLevelCache, CacheFactoryDirective cacheFactoryDirective, boolean foreignThreadAware);

	void registerFirstLevelCache(IWritableCache firstLevelCache, CacheFactoryDirective cacheFactoryDirective, boolean foreignThreadAware, String name);

	void unregisterFirstLevelCache(IWritableCache firstLevelCache, CacheFactoryDirective cacheFactoryDirective, boolean foreignThreadAware, String name);
}