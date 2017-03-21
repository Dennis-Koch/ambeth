package com.koch.ambeth.cache.server.ioc;

/*-
 * #%L
 * jambeth-cache-server
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

import com.koch.ambeth.cache.ioc.CacheModule;
import com.koch.ambeth.cache.server.CacheLocalDataChangeListener;
import com.koch.ambeth.cache.server.CacheService;
import com.koch.ambeth.cache.server.CacheServiceUtil;
import com.koch.ambeth.cache.server.DefaultPersistenceCacheRetriever;
import com.koch.ambeth.cache.server.ServiceResultHolder;
import com.koch.ambeth.cache.server.datachange.store.DataChangeEventStoreHandler;
import com.koch.ambeth.cache.service.ICacheRetriever;
import com.koch.ambeth.cache.service.ICacheService;
import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.datachange.model.IDataChangeOfSession;
import com.koch.ambeth.event.IEventTargetListenerExtendable;
import com.koch.ambeth.event.store.IEventStoreHandlerExtendable;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.persistence.IServiceUtil;
import com.koch.ambeth.service.cache.IServiceResultHolder;

@FrameworkModule
public class CacheServerModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(CacheModule.EXTERNAL_CACHE_SERVICE, CacheService.class).autowireable(ICacheService.class);

		beanContextFactory.registerBean("serviceResultHolder", ServiceResultHolder.class).autowireable(IServiceResultHolder.class);

		beanContextFactory.registerAutowireableBean(IServiceUtil.class, CacheServiceUtil.class);

		beanContextFactory.registerBean(CacheModule.DEFAULT_CACHE_RETRIEVER, DefaultPersistenceCacheRetriever.class).autowireable(ICacheRetriever.class);

		IBeanConfiguration cacheLocalDataChangeListener = beanContextFactory.registerBean(CacheLocalDataChangeListener.class).propertyRefs(
				CacheModule.CACHE_DATA_CHANGE_LISTENER);
		beanContextFactory.link(cacheLocalDataChangeListener).to(IEventTargetListenerExtendable.class).with(IDataChangeOfSession.class);

		IBeanConfiguration dataChangeEventStoreHandlerBC = beanContextFactory.registerBean(DataChangeEventStoreHandler.class);
		beanContextFactory.link(dataChangeEventStoreHandlerBC).to(IEventStoreHandlerExtendable.class).with(IDataChange.class).optional();
	}
}
