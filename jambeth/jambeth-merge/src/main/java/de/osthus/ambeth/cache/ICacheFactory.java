package de.osthus.ambeth.cache;

public interface ICacheFactory
{
	IDisposableCache createPrivileged(CacheFactoryDirective cacheFactoryDirective);

	IDisposableCache createPrivileged(CacheFactoryDirective cacheFactoryDirective, boolean foreignThreadAware, Boolean useWeakEntries);

	IDisposableCache create(CacheFactoryDirective cacheFactoryDirective);

	IDisposableCache create(CacheFactoryDirective cacheFactoryDirective, boolean foreignThreadAware, Boolean useWeakEntries);
}
