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

import java.lang.ref.Reference;

import com.koch.ambeth.merge.cache.CacheFactoryDirective;
import com.koch.ambeth.merge.cache.IWritableCache;

public class FlcEntry {
	protected final Reference<IWritableCache> firstLevelCacheR;

	protected final Reference<Thread> owningThreadR;

	protected final CacheFactoryDirective cacheFactoryDirective;

	public FlcEntry(CacheFactoryDirective cacheFactoryDirective,
			Reference<IWritableCache> firstLevelCacheR, Reference<Thread> owningThreadR) {
		this.cacheFactoryDirective = cacheFactoryDirective;
		this.firstLevelCacheR = firstLevelCacheR;
		this.owningThreadR = owningThreadR;
	}

	public CacheFactoryDirective getCacheFactoryDirective() {
		return cacheFactoryDirective;
	}

	public IWritableCache getFirstLevelCache() {
		return firstLevelCacheR != null ? firstLevelCacheR.get() : null;
	}

	public Thread getOwningThread() {
		return owningThreadR != null ? owningThreadR.get() : null;
	}

	public boolean isForeignThreadAware() {
		return owningThreadR == null;
	}

	public boolean isInterestedInThread(Thread thread) {
		Reference<Thread> owningThreadR = this.owningThreadR;
		if (owningThreadR == null) {
			return true;
		}
		Thread owningThread = owningThreadR.get();
		return thread.equals(owningThread);
	}
}
