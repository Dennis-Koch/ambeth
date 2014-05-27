package de.osthus.ambeth.filter.ioc;

import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.filter.FilterToQueryBuilder;
import de.osthus.ambeth.filter.IFilterToQueryBuilder;
import de.osthus.ambeth.filter.IQueryResultCache;
import de.osthus.ambeth.filter.QueryResultCache;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

@FrameworkModule
public class FilterPersistenceModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("filterToQueryBuilder", FilterToQueryBuilder.class).autowireable(IFilterToQueryBuilder.class);

		beanContextFactory.registerBean("queryResultCache", QueryResultCache.class).autowireable(IQueryResultCache.class);
		beanContextFactory.link("queryResultCache").to(IEventListenerExtendable.class).with(IDataChange.class);
		beanContextFactory.link("queryResultCache").to(IEventListenerExtendable.class).with(ClearAllCachesEvent.class);
	}
}
