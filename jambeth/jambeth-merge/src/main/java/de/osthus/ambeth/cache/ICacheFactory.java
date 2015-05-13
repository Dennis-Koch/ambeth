package de.osthus.ambeth.cache;

import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

public interface ICacheFactory
{
	IDisposableCache withParent(ICache parent, IResultingBackgroundWorkerDelegate<IDisposableCache> runnable);

	IDisposableCache createPrivileged(CacheFactoryDirective cacheFactoryDirective, String name);

	IDisposableCache createPrivileged(CacheFactoryDirective cacheFactoryDirective, boolean foreignThreadAware, Boolean useWeakEntries, String name);

	IDisposableCache create(CacheFactoryDirective cacheFactoryDirective, String name);

	IDisposableCache create(CacheFactoryDirective cacheFactoryDirective, boolean foreignThreadAware, Boolean useWeakEntries, String name);
}
