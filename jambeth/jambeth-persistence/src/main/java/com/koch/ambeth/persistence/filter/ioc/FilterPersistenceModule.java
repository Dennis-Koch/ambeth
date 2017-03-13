package com.koch.ambeth.persistence.filter.ioc;

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
