package com.koch.ambeth.cache.interceptor;

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

import java.lang.reflect.Method;

import com.koch.ambeth.cache.AbstractCache;
import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.cache.RootCache;
import com.koch.ambeth.cache.service.ICacheRetriever;
import com.koch.ambeth.ioc.IBeanRuntime;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.merge.cache.IWritableCache;
import com.koch.ambeth.service.IOfflineListenerExtendable;
import com.koch.ambeth.util.Lock;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.proxy.AbstractSimpleInterceptor;

public abstract class AbstractRootCacheAwareInterceptor extends AbstractSimpleInterceptor
		implements IThreadLocalCleanupBean {
	protected static final Method clearMethod;

	static {
		try {
			clearMethod = IWritableCache.class.getMethod("clear");
		}
		catch (SecurityException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		catch (NoSuchMethodException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Autowired(optional = true)
	protected IOfflineListenerExtendable offlineListenerExtendable;

	@Autowired
	protected IServiceContext serviceContext;

	@Autowired
	protected ICacheRetriever storedCacheRetriever;

	protected final IdentityHashSet<RootCache> allRootCaches = new IdentityHashSet<>();

	protected IRootCache acquireRootCache(boolean privileged,
			ThreadLocal<RootCache> currentRootCacheTL) {
		return acquireRootCache(privileged, currentRootCacheTL, storedCacheRetriever, null, null);
	}

	protected IRootCache acquireRootCache(boolean privileged,
			ThreadLocal<RootCache> currentRootCacheTL, ICacheRetriever cacheRetriever, Lock readLock,
			Lock writeLock) {
		IBeanRuntime<RootCache> rootCacheBR = serviceContext.registerBean(RootCache.class)//
				.propertyValue("CacheRetriever", cacheRetriever)//
				.propertyValue("BoundThread", Thread.currentThread());
		if (readLock != null) {
			rootCacheBR.propertyValue("ReadLock", readLock);
		}
		if (writeLock != null) {
			rootCacheBR.propertyValue("WriteLock", writeLock);
		}
		RootCache rootCache = postProcessRootCacheConfiguration(rootCacheBR)
				.propertyValue("Privileged", Boolean.valueOf(privileged)).finish();

		if (offlineListenerExtendable != null) {
			offlineListenerExtendable.addOfflineListener(rootCache);
		}
		synchronized (allRootCaches) {
			allRootCaches.add(rootCache);
		}
		currentRootCacheTL.set(rootCache);
		return rootCache;
	}

	protected IBeanRuntime<RootCache> postProcessRootCacheConfiguration(
			IBeanRuntime<RootCache> rootCacheBR) {
		// Do not inject EventQueue because caches without foreign interest will never receive async
		// DCEs
		return rootCacheBR.ignoreProperties(RootCache.P_EVENT_QUEUE)
				.propertyValue(AbstractCache.P_WEAK_ENTRIES, Boolean.FALSE);
	}

	protected void disposeCurrentRootCache(ThreadLocal<RootCache> currentTL) {
		RootCache rootCache = currentTL.get();
		currentTL.remove();
		if (rootCache == null) {
			return;
		}
		if (offlineListenerExtendable != null) {
			offlineListenerExtendable.removeOfflineListener(rootCache);
		}
		synchronized (allRootCaches) {
			allRootCaches.remove(rootCache);
		}
		// Cut reference to persistence layer
		rootCache.dispose();
	}
}
