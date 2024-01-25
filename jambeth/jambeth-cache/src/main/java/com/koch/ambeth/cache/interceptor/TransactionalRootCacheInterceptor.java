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

import com.koch.ambeth.cache.ICacheIntern;
import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.cache.ISecondLevelCacheManager;
import com.koch.ambeth.cache.ITransactionalRootCacheManager;
import com.koch.ambeth.cache.RootCache;
import com.koch.ambeth.cache.rootcachevalue.RootCacheValue;
import com.koch.ambeth.cache.service.ICacheRetriever;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.proxy.MethodProxy;

import java.lang.reflect.Method;

public class TransactionalRootCacheInterceptor extends AbstractRootCacheAwareInterceptor implements ITransactionalRootCacheManager, ISecondLevelCacheManager {
    protected static final Method clearMethod = AbstractRootCacheAwareInterceptor.clearMethod;
    @Forkable
    protected final ThreadLocal<RootCache> privilegedRootCacheTL = new ThreadLocal<>();
    @Forkable
    protected final ThreadLocal<RootCache> rootCacheTL = new ThreadLocal<>();
    @Forkable
    protected final ThreadLocal<Boolean> transactionalRootCacheActiveTL = new ThreadLocal<>();
    @Autowired
    protected IRootCache committedRootCache;
    @Autowired(optional = true)
    protected ISecurityActivation securityActivation;
    @Property(name = MergeConfigurationConstants.SecurityActive, defaultValue = "false")
    protected boolean securityActive;

    @Override
    public void cleanupThreadLocal() {
        transactionalRootCacheActiveTL.remove();
        disposeCurrentRootCache(privilegedRootCacheTL);
        disposeCurrentRootCache(rootCacheTL);
    }

    protected IRootCache getCurrentRootCache(boolean privileged) {
        return getCurrentRootCache(privileged, true);
    }

    protected IRootCache getCurrentRootCache(boolean privileged, boolean forceInstantiation) {
        IRootCache rootCache = privileged ? privilegedRootCacheTL.get() : rootCacheTL.get();
        if (rootCache != null) {
            return rootCache;
        }
        if (!Boolean.TRUE.equals(transactionalRootCacheActiveTL.get())) {
            // If no thread-bound root cache is active (which implies that no transaction is currently
            // active
            // return the unbound root cache (which reads committed data)
            return committedRootCache;
        }
        // if we need a cache and security is active the privileged cache is a prerequisite in both
        // cases
        IRootCache privilegedRootCache = privilegedRootCacheTL.get();
        if (privilegedRootCache == null) {
            if (!forceInstantiation) {
                // do not create an instance of anything in this case
                return null;
            }
            // here we know that the non-privileged one could not have existed before, so we simply create
            // the privileged one
            privilegedRootCache = acquireRootCache(true, privilegedRootCacheTL);
        }
        if (privileged) {
            // we need only the privilegedRootCache so we are finished
            return privilegedRootCache;
        }
        IRootCache nonPrivilegedRootCache = rootCacheTL.get();
        if (nonPrivilegedRootCache == null) {
            if (!forceInstantiation) {
                // do not create an instance of anything in this case
                return null;
            }
            // share the locks from the privileged rootCache
            nonPrivilegedRootCache = acquireRootCache(privileged, rootCacheTL, (ICacheRetriever) privilegedRootCache, privilegedRootCache.getReadLock(), privilegedRootCache.getWriteLock());
        }
        return nonPrivilegedRootCache;
    }

    @Override
    public IRootCache selectSecondLevelCache() {
        return getCurrentRootCache(isCurrentPrivileged());
    }

    @Override
    public IRootCache selectPrivilegedSecondLevelCache(boolean forceInstantiation) {
        return getCurrentRootCache(!securityActive || true, forceInstantiation);
    }

    @Override
    public IRootCache selectNonPrivilegedSecondLevelCache(boolean forceInstantiation) {
        return getCurrentRootCache(!securityActive || false, forceInstantiation);
    }

    protected boolean isCurrentPrivileged() {
        return !securityActive || !securityActivation.isFilterActivated();
    }

    @Override
    public void acquireTransactionalRootCache() {
        if (privilegedRootCacheTL.get() != null || privilegedRootCacheTL.get() != null) {
            throw new IllegalStateException("Transactional root cache already acquired");
        }
        transactionalRootCacheActiveTL.set(Boolean.TRUE);
    }

    @Override
    public void disposeTransactionalRootCache(boolean success) {
        transactionalRootCacheActiveTL.remove();

        disposeCurrentRootCache(rootCacheTL);

        var rootCache = privilegedRootCacheTL.get();
        if (rootCache == null) {
            disposeCurrentRootCache(privilegedRootCacheTL);
            // This may happen if an exception occurs while committing and therefore calling a rollback
            return;
        }
        try {
            if (success) {
                var content = new ArrayList<RootCacheValue>();

                // Save information into second level cache for committed data
                rootCache.getContent((entityType, idIndex, id, value) -> content.add((RootCacheValue) value));
                if (!content.isEmpty()) {
                    rootCache.clear();
                    committedRootCache.put(content);
                }
            }
        } finally {
            disposeCurrentRootCache(privilegedRootCacheTL);
        }
    }

    @Override
    protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        if (clearMethod.equals(method)) {
            var rootCache = privilegedRootCacheTL.get();
            if (rootCache != null) {
                rootCache.clear();
            }
            rootCache = rootCacheTL.get();
            if (rootCache != null) {
                rootCache.clear();
            }
            return null;
        }
        ICacheIntern requestingCache = null;
        for (var arg : args) {
            if (arg instanceof ICacheIntern) {
                requestingCache = (ICacheIntern) arg;
                break;
            }
        }
        boolean privileged;
        if (requestingCache != null) {
            privileged = requestingCache.isPrivileged();
        } else {
            privileged = isCurrentPrivileged();
        }
        var rootCache = getCurrentRootCache(privileged);
        return proxy.invoke(rootCache, args);
    }

    @Override
    public IRootCache[] selectAllCurrentSecondLevelCaches() {
        synchronized (allRootCaches) {
            return allRootCaches.toArray(IRootCache[]::new);
        }
    }
}
