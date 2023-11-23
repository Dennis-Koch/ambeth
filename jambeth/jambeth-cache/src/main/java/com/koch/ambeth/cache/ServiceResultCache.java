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

import com.koch.ambeth.cache.config.CacheConfigurationConstants;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.security.ISecurityManager;
import com.koch.ambeth.service.IServiceByNameProvider;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.service.cache.model.IServiceResult;
import com.koch.ambeth.service.cache.transfer.ServiceResult;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.model.IServiceDescription;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.state.StateRollback;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServiceResultCache implements IServiceResultCache {
    protected final HashMap<ServiceResultCacheKey, IServiceResult> serviceCallToResult = new HashMap<>();
    protected final HashSet<ServiceResultCacheKey> serviceCallToPendingResult = new HashSet<>();
    protected final Lock writeLock = new ReentrantLock();
    protected final Condition serviceCallPending = writeLock.newCondition();
    @Autowired
    protected IThreadLocalObjectCollector objectCollector;
    @Autowired(optional = true)
    protected ISecurityActivation securityActivation;
    @Autowired(optional = true)
    protected ISecurityManager securityManager;
    @Autowired
    protected IServiceByNameProvider serviceByNameProvider;
    @Property(name = CacheConfigurationConstants.ServiceResultCacheActive, defaultValue = "false")
    protected boolean useResultCache;

    protected ServiceResultCacheKey buildKey(IServiceDescription serviceDescription) {
        var service = serviceByNameProvider.getService(serviceDescription.getServiceName());
        var key = new ServiceResultCacheKey(serviceDescription.getMethod(service.getClass(), objectCollector), serviceDescription.getArguments(), serviceDescription.getServiceName());
        if (key.method == null || key.serviceName == null) {
            throw new IllegalArgumentException("ServiceDescription not legal " + serviceDescription);
        }
        return key;
    }

    @Override
    public IServiceResult getORIsOfService(final IServiceDescription serviceDescription, final ExecuteServiceDelegate executeServiceDelegate) {
        if (!useResultCache) {
            return executeServiceDelegate.invoke(serviceDescription);
        }
        var key = buildKey(serviceDescription);
        var writeLock = this.writeLock;
        IServiceResult serviceResult;
        writeLock.lock();
        try {
            serviceResult = serviceCallToResult.get(key);
            if (serviceResult != null) {
                // Important to clone the ori list, because potential
                // (user-dependent) security logic may truncate this list
                return createServiceResult(serviceResult);
            }
            while (serviceCallToPendingResult.contains(key)) {
                serviceCallPending.awaitUninterruptibly();
            }
            serviceResult = serviceCallToResult.get(key);
            if (serviceResult != null) {
                return createServiceResult(serviceResult);
            }
            serviceCallToPendingResult.add(key);
        } finally {
            writeLock.unlock();
        }
        var success = false;
        try {
            var rollback = securityActivation != null ? securityActivation.pushWithoutFiltering() : StateRollback.empty();
            try {
                serviceResult = executeServiceDelegate.invoke(serviceDescription);
                success = true;
            } finally {
                rollback.rollback();
            }
        } finally {
            writeLock.lock();
            try {
                serviceCallToPendingResult.remove(key);

                if (success) {
                    serviceCallToResult.put(key, serviceResult);
                }
                serviceCallPending.signalAll();
            } finally {
                writeLock.unlock();
            }
        }
        return createServiceResult(serviceResult);
    }

    protected IServiceResult createServiceResult(IServiceResult cachedServiceResult) {
        // Important to clone the ori list, because potential (user-dependent)
        // security logic may truncate this list (original must remain unmodified)
        var objRefs = cachedServiceResult.getObjRefs();
        var list = new ArrayList<IObjRef>(objRefs.size());
        list.addAll(objRefs);

        List<IObjRef> filteredList;
        if (securityManager != null) {
            filteredList = securityManager.filterValue(list);
        } else {
            filteredList = list;
        }
        var serviceResult = new ServiceResult();
        serviceResult.setAdditionalInformation(cachedServiceResult.getAdditionalInformation());
        serviceResult.setObjRefs(filteredList);
        return serviceResult;
    }

    public void handleClearAllCaches(ClearAllCachesEvent evnt) {
        invalidateAll();
    }

    @Override
    public void invalidateAll() {
        writeLock.lock();
        try {
            serviceCallToResult.clear();
        } finally {
            writeLock.unlock();
        }
    }
}
