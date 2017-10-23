package com.koch.ambeth.cache.datachange.ioc;

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

import com.koch.ambeth.cache.datachange.CacheDataChangeListener;
import com.koch.ambeth.cache.datachange.DataChangeEventBatcher;
import com.koch.ambeth.cache.datachange.RootCacheClearEventListener;
import com.koch.ambeth.cache.datachange.ServiceResultCacheDCL;
import com.koch.ambeth.cache.datachange.revert.RevertChangesHelper;
import com.koch.ambeth.cache.ioc.CacheModule;
import com.koch.ambeth.datachange.UnfilteredDataChangeListener;
import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.event.IEventBatcherExtendable;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.event.IEventTargetListenerExtendable;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.IRevertChangesHelper;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;

@FrameworkModule
public class CacheDataChangeModule implements IInitializingModule {
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		beanContextFactory.registerBean(RevertChangesHelper.class)
				.autowireable(IRevertChangesHelper.class);

		IBeanConfiguration rootCacheClearEventListenerBC =
				beanContextFactory.registerBean(RootCacheClearEventListener.class)
						.propertyRefs(CacheModule.COMMITTED_ROOT_CACHE);

		beanContextFactory.link(rootCacheClearEventListenerBC).to(IEventListenerExtendable.class)
				.with(ClearAllCachesEvent.class);

		IBeanConfiguration serviceResultCacheDCL =
				beanContextFactory.registerBean(UnfilteredDataChangeListener.class)
						.propertyRef(beanContextFactory.registerBean(ServiceResultCacheDCL.class));
		beanContextFactory.link(serviceResultCacheDCL).to(IEventListenerExtendable.class)
				.with(IDataChange.class);

		IBeanConfiguration cacheDataChangeListener =
				beanContextFactory.registerBean(CacheModule.CACHE_DATA_CHANGE_LISTENER,
						CacheDataChangeListener.class);

		beanContextFactory.link(cacheDataChangeListener).to(IEventTargetListenerExtendable.class)
				.with(IDataChange.class);

		IBeanConfiguration dataChangeEventBatcher =
				beanContextFactory.registerBean(DataChangeEventBatcher.class);
		beanContextFactory.link(dataChangeEventBatcher).to(IEventBatcherExtendable.class)
				.with(IDataChange.class);
	}
}
