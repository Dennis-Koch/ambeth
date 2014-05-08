package de.osthus.ambeth.cache;

public interface ICacheFactory
{
	IDisposableCache create(CacheFactoryDirective cacheFactoryDirective);

	IDisposableCache create(CacheFactoryDirective cacheFactoryDirective, boolean foreignThreadAware, Boolean useWeakEntries);
}
