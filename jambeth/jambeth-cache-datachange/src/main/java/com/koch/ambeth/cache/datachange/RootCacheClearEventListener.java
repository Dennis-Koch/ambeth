package com.koch.ambeth.cache.datachange;

/*-
 * #%L
 * jambeth-cache-datachange
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

import com.koch.ambeth.cache.IFirstLevelCacheManager;
import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.cache.ISecondLevelCacheManager;
import com.koch.ambeth.event.IEventListener;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;

public class RootCacheClearEventListener implements IEventListener {
    @Autowired
    protected IFirstLevelCacheManager firstLevelCacheManager;

    @Autowired
    protected ISecondLevelCacheManager secondLevelCacheManager;

    @Autowired
    protected IRootCache committedRootCache;

    @Override
    public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) {
        if (!(eventObject instanceof ClearAllCachesEvent)) {
            return;
        }
        committedRootCache.clear();
        var privilegedSecondLevelCache = secondLevelCacheManager.selectPrivilegedSecondLevelCache(false);
        if (privilegedSecondLevelCache != null && privilegedSecondLevelCache != committedRootCache) {
            privilegedSecondLevelCache.clear();
        }
        var nonPrivilegedSecondLevelCache = secondLevelCacheManager.selectNonPrivilegedSecondLevelCache(false);
        if (nonPrivilegedSecondLevelCache != null && nonPrivilegedSecondLevelCache != committedRootCache && nonPrivilegedSecondLevelCache != privilegedSecondLevelCache) {
            nonPrivilegedSecondLevelCache.clear();
        }
        var firstLevelCaches = firstLevelCacheManager.selectFirstLevelCaches();
        for (int a = firstLevelCaches.size(); a-- > 0; ) {
            var firstLevelCache = firstLevelCaches.get(a);
            firstLevelCache.clear();
        }
    }
}
