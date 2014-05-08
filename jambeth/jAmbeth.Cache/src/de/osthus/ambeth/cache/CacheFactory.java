package de.osthus.ambeth.cache;

import de.osthus.ambeth.ioc.IBeanRuntime;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ParamChecker;

public class CacheFactory implements ICacheFactory, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IServiceContext beanContext;

	protected IFirstLevelCacheExtendable firstLevelCacheExtendable;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(beanContext, "BeanContext");
		ParamChecker.assertNotNull(firstLevelCacheExtendable, "FirstLevelCacheExtendable");
	}

	public void setBeanContext(IServiceContext beanContext)
	{
		this.beanContext = beanContext;
	}

	public void setFirstLevelCacheExtendable(IFirstLevelCacheExtendable firstLevelCacheExtendable)
	{
		this.firstLevelCacheExtendable = firstLevelCacheExtendable;
	}

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