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
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.cache.IWritableCache;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.IList;

public class RootCacheClearEventListener implements IEventListener, IInitializingBean {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IFirstLevelCacheManager firstLevelCacheManager;

	protected ISecondLevelCacheManager secondLevelCacheManager;

	protected IRootCache committedRootCache;

	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(firstLevelCacheManager, "FirstLevelCacheManager");
		ParamChecker.assertNotNull(secondLevelCacheManager, "SecondLevelCacheManager");
		ParamChecker.assertNotNull(committedRootCache, "CommittedRootCache");
	}

	public void setFirstLevelCacheManager(IFirstLevelCacheManager firstLevelCacheManager) {
		this.firstLevelCacheManager = firstLevelCacheManager;
	}

	public void setSecondLevelCacheManager(ISecondLevelCacheManager secondLevelCacheManager) {
		this.secondLevelCacheManager = secondLevelCacheManager;
	}

	public void setCommittedRootCache(IRootCache committedRootCache) {
		this.committedRootCache = committedRootCache;
	}

	@Override
	public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) {
		if (!(eventObject instanceof ClearAllCachesEvent)) {
			return;
		}
		committedRootCache.clear();
		IRootCache privilegedSecondLevelCache =
				secondLevelCacheManager.selectPrivilegedSecondLevelCache(false);
		if (privilegedSecondLevelCache != null && privilegedSecondLevelCache != committedRootCache) {
			privilegedSecondLevelCache.clear();
		}
		IRootCache nonPrivilegedSecondLevelCache =
				secondLevelCacheManager.selectNonPrivilegedSecondLevelCache(false);
		if (nonPrivilegedSecondLevelCache != null && nonPrivilegedSecondLevelCache != committedRootCache
				&& nonPrivilegedSecondLevelCache != privilegedSecondLevelCache) {
			nonPrivilegedSecondLevelCache.clear();
		}
		IList<IWritableCache> firstLevelCaches = firstLevelCacheManager.selectFirstLevelCaches();
		for (int a = firstLevelCaches.size(); a-- > 0;) {
			IWritableCache firstLevelCache = firstLevelCaches.get(a);
			firstLevelCache.clear();
		}
	}
}
