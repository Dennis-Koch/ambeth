package com.koch.ambeth.persistence.filter.ioc;

/*-
 * #%L
 * jambeth-persistence
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

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.datachange.model.IDataChangeOfSession;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.event.IDatabaseReleaseEvent;
import com.koch.ambeth.persistence.filter.FilterToQueryBuilder;
import com.koch.ambeth.persistence.filter.QueryResultCache;
import com.koch.ambeth.query.filter.IFilterToQueryBuilder;
import com.koch.ambeth.query.filter.IQueryResultCache;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;

@FrameworkModule
public class FilterPersistenceModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(FilterToQueryBuilder.class).autowireable(IFilterToQueryBuilder.class);

		IBeanConfiguration queryResultCache = beanContextFactory.registerBean(QueryResultCache.class).autowireable(IQueryResultCache.class);
		beanContextFactory.link(queryResultCache, "handleClearAllCaches").to(IEventListenerExtendable.class).with(ClearAllCachesEvent.class);
		beanContextFactory.link(queryResultCache, "handleDatabaseRelease").to(IEventListenerExtendable.class).with(IDatabaseReleaseEvent.class);
		beanContextFactory.link(queryResultCache, "handleDataChange").to(IEventListenerExtendable.class).with(IDataChange.class);
		beanContextFactory.link(queryResultCache, "handleDataChangeOfSession").to(IEventListenerExtendable.class).with(IDataChangeOfSession.class);
	}
}
