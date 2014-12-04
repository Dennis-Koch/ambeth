package de.osthus.ambeth.cache;

public interface ICacheFactory
{
	IDisposableCache createPrivileged(CacheFactoryDirective cacheFactoryDirective, String name);

	IDisposableCache createPrivileged(CacheFactoryDirective cacheFactoryDirective, boolean foreignThreadAware, Boolean useWeakEntries, String name);

	IDisposableCache create(CacheFactoryDirective cacheFactoryDirective, String name);

	IDisposableCache create(CacheFactoryDirective cacheFactoryDirective, boolean foreignThreadAware, Boolean useWeakEntries, String name);
}
