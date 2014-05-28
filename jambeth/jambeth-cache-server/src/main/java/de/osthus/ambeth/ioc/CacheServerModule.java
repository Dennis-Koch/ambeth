package de.osthus.ambeth.ioc;

import de.osthus.ambeth.cache.CacheLocalDataChangeListener;
import de.osthus.ambeth.cache.CacheService;
import de.osthus.ambeth.cache.CacheServiceUtil;
import de.osthus.ambeth.cache.DefaultPersistenceCacheRetriever;
import de.osthus.ambeth.cache.IServiceResultHolder;
import de.osthus.ambeth.cache.ServiceResultHolder;
import de.osthus.ambeth.cache.config.CacheConfigurationConstants;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.datachange.store.DataChangeEventStoreHandler;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.event.store.IEventStoreHandlerExtendable;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.event.LocalDataChangeEvent;
import de.osthus.ambeth.persistence.IServiceUtil;
import de.osthus.ambeth.service.ICacheRetriever;
import de.osthus.ambeth.service.ICacheService;

@FrameworkModule
public class CacheServerModule implements IInitializingModule
{
	protected boolean cacheServiceRegistryActive;

	@Property(name = CacheConfigurationConstants.CacheServiceRegistryActive, defaultValue = "true")
	public void setCacheServiceRegistryActive(boolean cacheServiceRegistryActive)
	{
		this.cacheServiceRegistryActive = cacheServiceRegistryActive;
	}

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(CacheModule.EXTERNAL_CACHE_SERVICE, CacheService.class).autowireable(ICacheService.class);

		beanContextFactory.registerBean("serviceResultHolder", ServiceResultHolder.class).autowireable(IServiceResultHolder.class);

		beanContextFactory.registerAutowireableBean(IServiceUtil.class, CacheServiceUtil.class);

		beanContextFactory.registerBean(CacheModule.DEFAULT_CACHE_RETRIEVER, DefaultPersistenceCacheRetriever.class).autowireable(ICacheRetriever.class);
		if (!cacheServiceRegistryActive)
		{
			beanContextFactory.registerAlias(CacheModule.ROOT_CACHE_RETRIEVER, CacheModule.DEFAULT_CACHE_RETRIEVER);
		}

		beanContextFactory.registerBean("cacheLocalDataChangeListener", CacheLocalDataChangeListener.class)
				.propertyRefs(CacheModule.CACHE_DATA_CHANGE_LISTENER);
		beanContextFactory.link("cacheLocalDataChangeListener").to(IEventListenerExtendable.class).with(LocalDataChangeEvent.class);

		IBeanConfiguration dataChangeEventStoreHandlerBC = beanContextFactory.registerAnonymousBean(DataChangeEventStoreHandler.class);
		beanContextFactory.link(dataChangeEventStoreHandlerBC).to(IEventStoreHandlerExtendable.class).with(IDataChange.class).optional();
	}
}
