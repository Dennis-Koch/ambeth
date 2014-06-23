package de.osthus.ambeth.cache;

import de.osthus.ambeth.ioc.IBeanRuntime;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class CacheFactory implements ICacheFactory
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IFirstLevelCacheExtendable firstLevelCacheExtendable;

	@Override
	public IDisposableCache create(CacheFactoryDirective cacheFactoryDirective)
	{
		return create(cacheFactoryDirective, false, null);
	}

	@Override
	public IDisposableCache create(CacheFactoryDirective cacheFactoryDirective, boolean foreignThreadAware, Boolean useWeakEntries)
	{
		IBeanRuntime<ChildCache> firstLevelCacheBC = beanContext.registerAnonymousBean(ChildCache.class);
		if (!foreignThreadAware)
		{
			// Do not inject EventQueue because caches without foreign interest will never receive async DCEs
			firstLevelCacheBC.ignoreProperties("EventQueue");
		}
		if (useWeakEntries != null)
		{
			firstLevelCacheBC.propertyValue("WeakEntries", useWeakEntries);
		}
		ChildCache firstLevelCache = firstLevelCacheBC.finish();
		firstLevelCacheExtendable.registerFirstLevelCache(firstLevelCache, cacheFactoryDirective, foreignThreadAware);
		return firstLevelCache;
	}
}