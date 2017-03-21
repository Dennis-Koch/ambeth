package com.koch.ambeth.testutil;

/*-
 * #%L
 * jambeth-information-bus-test
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

import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.cache.ioc.CacheModule;
import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.util.setup.IDataSetup;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;

public class CleanupAfterInformationBus extends CleanupAfterIoc {
	@Autowired(CacheModule.COMMITTED_ROOT_CACHE)
	protected IRootCache committedRootCache;

	@Autowired(optional = true)
	protected IDataSetup dataSetup;

	@Autowired
	protected IEventDispatcher eventDispatcher;

	@Override
	public void cleanup() {
		if (dataSetup != null) {
			dataSetup.eraseEntityReferences();
		}
		committedRootCache.clear();
		eventDispatcher.dispatchEvent(ClearAllCachesEvent.getInstance());

		super.cleanup();
	}
}
