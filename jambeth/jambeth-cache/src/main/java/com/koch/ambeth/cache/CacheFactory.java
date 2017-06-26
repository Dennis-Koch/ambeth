package com.koch.ambeth.cache;

/*-
 * #%L
 * jambeth-cache
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.ioc.IBeanRuntime;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.cache.CacheFactoryDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.merge.cache.IDisposableCache;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

public class CacheFactory implements ICacheFactory {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IFirstLevelCacheExtendable firstLevelCacheExtendable;

	@Property(name = MergeConfigurationConstants.SecurityActive, defaultValue = "false")
	protected boolean securityActive;

	protected final ThreadLocal<ICache> parentTL = new ThreadLocal<>();

	@Override
	public IDisposableCache withParent(ICache parent,
			IResultingBackgroundWorkerDelegate<IDisposableCache> runnable) {
		ICache oldParent = parentTL.get();
		parentTL.set(parent);
		try {
			return runnable.invoke();
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			parentTL.set(oldParent);
		}
	}

	@Override
	public IDisposableCache create(CacheFactoryDirective cacheFactoryDirective, String name) {
		if (!securityActive) {
			return createPrivileged(cacheFactoryDirective, name);
		}
		return createIntern(cacheFactoryDirective, false, false, null, name);
	}

	@Override
	public IDisposableCache createPrivileged(CacheFactoryDirective cacheFactoryDirective,
			String name) {
		return createIntern(cacheFactoryDirective, true, false, null, name);
	}

	@Override
	public IDisposableCache create(CacheFactoryDirective cacheFactoryDirective,
			boolean foreignThreadAware, Boolean useWeakEntries, String name) {
		if (!securityActive) {
			return createPrivileged(cacheFactoryDirective, foreignThreadAware, useWeakEntries, name);
		}
		return createIntern(cacheFactoryDirective, false, foreignThreadAware, useWeakEntries, name);
	}

	@Override
	public IDisposableCache createPrivileged(CacheFactoryDirective cacheFactoryDirective,
			boolean foreignThreadAware, Boolean useWeakEntries, String name) {
		return createIntern(cacheFactoryDirective, true, foreignThreadAware, useWeakEntries, name);
	}

	protected IDisposableCache createIntern(CacheFactoryDirective cacheFactoryDirective,
			boolean privileged, boolean foreignThreadAware, Boolean useWeakEntries, String name) {
		IBeanRuntime<ChildCache> firstLevelCacheBC = beanContext.registerBean(ChildCache.class);
		if (!foreignThreadAware) {
			// Do not inject EventQueue because caches without foreign interest will never receive async
			// DCEs
			firstLevelCacheBC.ignoreProperties(ChildCache.P_EVENT_QUEUE);
		}
		if (useWeakEntries != null) {
			firstLevelCacheBC.propertyValue(AbstractCache.P_WEAK_ENTRIES, useWeakEntries);
		}
		if (name != null) {
			firstLevelCacheBC.propertyValue(ChildCache.P_NAME, name);
		}
		ICache parent = parentTL.get();
		if (parent != null) {
			firstLevelCacheBC.propertyValue(ChildCache.P_PARENT, parent);
		}
		firstLevelCacheBC.propertyValue(ChildCache.P_PRIVILEGED, Boolean.valueOf(privileged));
		ChildCache firstLevelCache = firstLevelCacheBC.finish();
		firstLevelCacheExtendable.registerFirstLevelCache(firstLevelCache, cacheFactoryDirective,
				foreignThreadAware, name);
		return firstLevelCache;
	}
}
