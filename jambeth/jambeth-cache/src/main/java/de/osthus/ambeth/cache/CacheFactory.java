package de.osthus.ambeth.cache;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IBeanRuntime;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.security.config.SecurityConfigurationConstants;

public class CacheFactory implements ICacheFactory
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IFirstLevelCacheExtendable firstLevelCacheExtendable;

	@Autowired(optional = true)
	protected ISecurityActivation securityActivation;

	@Property(name = SecurityConfigurationConstants.SecurityActive, defaultValue = "false")
	protected boolean securityActive;

	@Override
	public IDisposableCache create(CacheFactoryDirective cacheFactoryDirective, String name)
	{
		return createIntern(cacheFactoryDirective, !securityActive || !securityActivation.isFilterActivated(), false, null, name);
	}

	@Override
	public IDisposableCache createPrivileged(CacheFactoryDirective cacheFactoryDirective, String name)
	{
		return createIntern(cacheFactoryDirective, true, false, null, name);
	}

	@Override
	public IDisposableCache create(CacheFactoryDirective cacheFactoryDirective, boolean foreignThreadAware, Boolean useWeakEntries, String name)
	{
		return createIntern(cacheFactoryDirective, !securityActive || !securityActivation.isFilterActivated(), foreignThreadAware, useWeakEntries, name);
	}

	@Override
	public IDisposableCache createPrivileged(CacheFactoryDirective cacheFactoryDirective, boolean foreignThreadAware, Boolean useWeakEntries, String name)
	{
		return createIntern(cacheFactoryDirective, true, foreignThreadAware, useWeakEntries, name);
	}

	protected IDisposableCache createIntern(CacheFactoryDirective cacheFactoryDirective, boolean privileged, boolean foreignThreadAware,
			Boolean useWeakEntries, String name)
	{
		IBeanRuntime<ChildCache> firstLevelCacheBC = beanContext.registerBean(ChildCache.class);
		if (!foreignThreadAware)
		{
			// Do not inject EventQueue because caches without foreign interest will never receive async DCEs
			firstLevelCacheBC.ignoreProperties("EventQueue");
		}
		if (useWeakEntries != null)
		{
			firstLevelCacheBC.propertyValue("WeakEntries", useWeakEntries);
		}
		if (name != null)
		{
			firstLevelCacheBC.propertyValue("Name", name);
		}
		firstLevelCacheBC.propertyValue("Privileged", Boolean.valueOf(privileged));
		ChildCache firstLevelCache = firstLevelCacheBC.finish();
		firstLevelCacheExtendable.registerFirstLevelCache(firstLevelCache, cacheFactoryDirective, foreignThreadAware, name);
		return firstLevelCache;
	}
}