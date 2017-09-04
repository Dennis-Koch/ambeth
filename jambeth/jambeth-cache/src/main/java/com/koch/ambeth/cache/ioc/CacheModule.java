package com.koch.ambeth.cache.ioc;

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

import com.koch.ambeth.cache.CacheEventTargetExtractor;
import com.koch.ambeth.cache.CacheFactory;
import com.koch.ambeth.cache.CacheProvider;
import com.koch.ambeth.cache.CacheRetrieverRegistry;
import com.koch.ambeth.cache.CacheType;
import com.koch.ambeth.cache.FirstLevelCacheManager;
import com.koch.ambeth.cache.ICacheIntern;
import com.koch.ambeth.cache.ICacheProviderExtendable;
import com.koch.ambeth.cache.IFirstLevelCacheExtendable;
import com.koch.ambeth.cache.IFirstLevelCacheManager;
import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.cache.ISecondLevelCacheManager;
import com.koch.ambeth.cache.IServiceResultCache;
import com.koch.ambeth.cache.ITransactionalRootCacheManager;
import com.koch.ambeth.cache.PagingQueryServiceResultProcessor;
import com.koch.ambeth.cache.RootCache;
import com.koch.ambeth.cache.RootCacheBridge;
import com.koch.ambeth.cache.ServiceResultCache;
import com.koch.ambeth.cache.ValueHolderIEC;
import com.koch.ambeth.cache.collections.CacheMapEntryTypeProvider;
import com.koch.ambeth.cache.collections.ICacheMapEntryTypeProvider;
import com.koch.ambeth.cache.config.CacheConfigurationConstants;
import com.koch.ambeth.cache.config.CacheNamedBeans;
import com.koch.ambeth.cache.databinding.ICollectionChangeExtensionExtendable;
import com.koch.ambeth.cache.databinding.IPropertyChangeExtensionExtendable;
import com.koch.ambeth.cache.interceptor.CacheProviderInterceptor;
import com.koch.ambeth.cache.interceptor.ThreadLocalRootCacheInterceptor;
import com.koch.ambeth.cache.interceptor.TransactionalRootCacheInterceptor;
import com.koch.ambeth.cache.mixin.DataObjectMixin;
import com.koch.ambeth.cache.mixin.EmbeddedTypeMixin;
import com.koch.ambeth.cache.mixin.EntityEqualsMixin;
import com.koch.ambeth.cache.mixin.IAsyncLazyLoadController;
import com.koch.ambeth.cache.mixin.IPropertyChangeItemListenerExtendable;
import com.koch.ambeth.cache.mixin.PropertyChangeMixin;
import com.koch.ambeth.cache.mixin.ValueHolderContainerMixin;
import com.koch.ambeth.cache.proxy.CacheContextPostProcessor;
import com.koch.ambeth.cache.proxy.CachePostProcessor;
import com.koch.ambeth.cache.rootcachevalue.IRootCacheValueFactory;
import com.koch.ambeth.cache.rootcachevalue.RootCacheValueFactory;
import com.koch.ambeth.cache.service.ICacheRetrieverExtendable;
import com.koch.ambeth.cache.service.ICacheService;
import com.koch.ambeth.cache.service.ICacheServiceByNameExtendable;
import com.koch.ambeth.cache.service.IPrimitiveRetrieverExtendable;
import com.koch.ambeth.cache.service.IRelationRetrieverExtendable;
import com.koch.ambeth.cache.util.CacheHelper;
import com.koch.ambeth.cache.util.ICachePathHelper;
import com.koch.ambeth.cache.util.IPrioMembersProvider;
import com.koch.ambeth.cache.util.PrioMembersProvider;
import com.koch.ambeth.cache.walker.CacheWalker;
import com.koch.ambeth.cache.walker.ICacheWalker;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.event.IEventTargetExtractorExtendable;
import com.koch.ambeth.event.events.EventSessionChanged;
import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.filter.query.service.IGenericQueryService;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.IProxyHelper;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheContext;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.merge.cache.ICacheProvider;
import com.koch.ambeth.merge.cache.IWritableCache;
import com.koch.ambeth.merge.event.IEntityMetaDataEvent;
import com.koch.ambeth.merge.util.ICacheHelper;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.service.IOfflineListener;
import com.koch.ambeth.service.IOfflineListenerExtendable;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.service.cache.IServiceResultProcessorExtendable;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.remote.ClientServiceBean;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.proxy.IProxyFactory;

import net.sf.cglib.proxy.MethodInterceptor;

@FrameworkModule
public class CacheModule implements IInitializingModule {
	public static final String CACHE_DATA_CHANGE_LISTENER = "cache.dcl";

	public static final String COMMITTED_ROOT_CACHE = "committedRootCache";

	public static final String INTERNAL_CACHE_SERVICE = "cacheService.internal";

	public static final String EXTERNAL_CACHE_SERVICE = "cacheService.external";

	public static final String DEFAULT_CACHE_RETRIEVER = "cacheRetriever.default";

	public static final String ROOT_CACHE_RETRIEVER = "cacheRetriever.rootCache";

	public static final String ROOT_CACHE = "rootCache";

	@Property(name = ServiceConfigurationConstants.NetworkClientMode, defaultValue = "false")
	protected boolean isNetworkClientMode;

	@Property(name = CacheConfigurationConstants.CacheServiceBeanActive, defaultValue = "true")
	protected boolean isCacheServiceBeanActive;

	@Autowired
	protected IProxyFactory proxyFactory;

	@Property(name = CacheConfigurationConstants.FirstLevelCacheType, mandatory = false)
	protected CacheType defaultCacheType;

	@Property(name = CacheConfigurationConstants.SecondLevelCacheActive, defaultValue = "true")
	protected boolean secondLevelCacheActive;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		if (defaultCacheType == null) {
			defaultCacheType = CacheType.DEFAULT;
		}
		ParamChecker.assertNotNull(proxyFactory, "proxyFactory");

		IBeanConfiguration serviceResultcache = beanContextFactory
				.registerBean(ServiceResultCache.class).autowireable(IServiceResultCache.class);
		beanContextFactory.link(serviceResultcache, "handleClearAllCaches")
				.to(IEventListenerExtendable.class).with(ClearAllCachesEvent.class);

		beanContextFactory.registerBean(ValueHolderIEC.class).autowireable(ValueHolderIEC.class,
				IProxyHelper.class);

		beanContextFactory.registerBean(CacheHelper.class).autowireable(ICacheHelper.class,
				ICachePathHelper.class, IPrefetchHelper.class);

		IBeanConfiguration prioMembersProvider = beanContextFactory
				.registerBean(PrioMembersProvider.class).autowireable(IPrioMembersProvider.class);
		beanContextFactory.link(prioMembersProvider, PrioMembersProvider.handleMetaDataAddedEvent)
				.to(IEventListenerExtendable.class).with(IEntityMetaDataEvent.class);

		beanContextFactory.registerBean(CacheWalker.class).autowireable(ICacheWalker.class);

		beanContextFactory.registerAutowireableBean(ICacheMapEntryTypeProvider.class,
				CacheMapEntryTypeProvider.class);

		beanContextFactory.registerAutowireableBean(IRootCacheValueFactory.class,
				RootCacheValueFactory.class);

		IBeanConfiguration cacheRetrieverRegistry = beanContextFactory
				.registerBean(ROOT_CACHE_RETRIEVER, CacheRetrieverRegistry.class)
				.autowireable(ICacheServiceByNameExtendable.class, ICacheRetrieverExtendable.class,
						IRelationRetrieverExtendable.class, IPrimitiveRetrieverExtendable.class);
		beanContextFactory
				.link(cacheRetrieverRegistry, CacheRetrieverRegistry.HANDLE_EVENT_SESSION_CHANGED)
				.to(IEventListenerExtendable.class).with(EventSessionChanged.class);

		beanContextFactory.registerBean("firstLevelCacheManager", FirstLevelCacheManager.class)
				.autowireable(IFirstLevelCacheExtendable.class, IFirstLevelCacheManager.class);

		String rootCacheBridge = "rootCacheBridge";

		beanContextFactory.registerBean(rootCacheBridge, RootCacheBridge.class)
				.propertyRefs(COMMITTED_ROOT_CACHE, ROOT_CACHE_RETRIEVER);

		TransactionalRootCacheInterceptor txRcInterceptor = new TransactionalRootCacheInterceptor();

		beanContextFactory.registerWithLifecycle("txRootCacheInterceptor", txRcInterceptor)
				.propertyRefs(COMMITTED_ROOT_CACHE, rootCacheBridge)
				.autowireable(ITransactionalRootCacheManager.class, ISecondLevelCacheManager.class);

		Object txRcProxy = proxyFactory.createProxy(
				new Class<?>[] {IRootCache.class, ICacheIntern.class, IOfflineListener.class},
				txRcInterceptor);

		beanContextFactory.registerExternalBean(ROOT_CACHE, txRcProxy).autowireable(IRootCache.class,
				ICacheIntern.class);

		if (secondLevelCacheActive) {
			// One single root cache instance for whole context
			beanContextFactory.registerBean(COMMITTED_ROOT_CACHE, RootCache.class)
					.propertyRef("CacheRetriever", ROOT_CACHE_RETRIEVER)
					.propertyValue("Privileged", Boolean.TRUE);
			beanContextFactory.link(COMMITTED_ROOT_CACHE).to(IOfflineListenerExtendable.class);
		}
		else {
			// One root cache instance per thread sequence. Most often used in server environment where
			// the "deactivated"
			// second level cache means that each thread hold his own, isolated root cache (which gets
			// cleared with each service
			// request. Effectively this means that the root cache itself only lives per-request and does
			// not hold a longer state
			ThreadLocalRootCacheInterceptor threadLocalRcInterceptor =
					new ThreadLocalRootCacheInterceptor();

			beanContextFactory
					.registerWithLifecycle("threadLocalRootCacheInterceptor", threadLocalRcInterceptor)
					.propertyRef("StoredCacheRetriever", CacheModule.ROOT_CACHE_RETRIEVER)
					.propertyValue("Privileged", Boolean.TRUE);

			IRootCache threadLocalRcProxy = proxyFactory.createProxy(IRootCache.class,
					threadLocalRcInterceptor);

			beanContextFactory.registerExternalBean(COMMITTED_ROOT_CACHE, threadLocalRcProxy);
		}
		beanContextFactory.registerBean("cacheEventTargetExtractor", CacheEventTargetExtractor.class);
		beanContextFactory.link("cacheEventTargetExtractor").to(IEventTargetExtractorExtendable.class)
				.with(ICache.class).optional();

		beanContextFactory.registerBean(CacheFactory.class).autowireable(ICacheFactory.class);

		MethodInterceptor cacheProviderInterceptor = (MethodInterceptor) beanContextFactory
				.registerBean("cacheProviderInterceptor", CacheProviderInterceptor.class)
				.autowireable(ICacheProviderExtendable.class, ICacheProvider.class, ICacheContext.class)
				.getInstance();

		Object cacheProxy = proxyFactory.createProxy(ICache.class,
				new Class[] {ICacheProvider.class, IWritableCache.class}, cacheProviderInterceptor);
		beanContextFactory.registerExternalBean("cache", cacheProxy).autowireable(ICache.class);

		beanContextFactory.registerBean("pagingQuerySRP", PagingQueryServiceResultProcessor.class);
		beanContextFactory.link("pagingQuerySRP").to(IServiceResultProcessorExtendable.class)
				.with(IPagingResponse.class);

		beanContextFactory.registerBean(CacheNamedBeans.CacheProviderSingleton, CacheProvider.class)
				.propertyValue("CacheType", CacheType.SINGLETON);

		beanContextFactory.registerBean(CacheNamedBeans.CacheProviderThreadLocal, CacheProvider.class)
				.propertyValue("CacheType", CacheType.THREAD_LOCAL);

		beanContextFactory.registerBean(CacheNamedBeans.CacheProviderPrototype, CacheProvider.class)
				.propertyValue("CacheType", CacheType.PROTOTYPE);

		String defaultCacheProviderBeanName;
		switch (defaultCacheType) {
			case PROTOTYPE: {
				defaultCacheProviderBeanName = CacheNamedBeans.CacheProviderPrototype;
				break;
			}
			case SINGLETON: {
				defaultCacheProviderBeanName = CacheNamedBeans.CacheProviderSingleton;
				break;
			}
			case DEFAULT:
			case THREAD_LOCAL: {
				defaultCacheProviderBeanName = CacheNamedBeans.CacheProviderThreadLocal;
				break;
			}
			default:
				throw new IllegalStateException(
						"Unsupported " + CacheType.class.getName() + ": " + defaultCacheType);
		}
		beanContextFactory.link(defaultCacheProviderBeanName).to(ICacheProviderExtendable.class);

		// CacheContextPostProcessor must be registered AFTER CachePostProcessor...
		Object cachePostProcessor = beanContextFactory.registerBean(CachePostProcessor.class)
				.getInstance();
		beanContextFactory.registerBean(CacheContextPostProcessor.class)
				.propertyValue("CachePostProcessor", cachePostProcessor);

		if (isNetworkClientMode) {
			beanContextFactory.registerBean(ClientServiceBean.class)
					.propertyValue(ClientServiceBean.INTERFACE_PROP_NAME, IGenericQueryService.class)
					.autowireable(IGenericQueryService.class);

			if (isCacheServiceBeanActive) {
				IBeanConfiguration remoteCacheService = beanContextFactory
						.registerBean(CacheModule.EXTERNAL_CACHE_SERVICE, ClientServiceBean.class)
						.propertyValue(ClientServiceBean.INTERFACE_PROP_NAME, ICacheService.class)
						.autowireable(ICacheService.class);

				beanContextFactory.registerAlias(CacheModule.DEFAULT_CACHE_RETRIEVER,
						CacheModule.EXTERNAL_CACHE_SERVICE);

				// register to all entities in a "most-weak" manner
				beanContextFactory.link(remoteCacheService).to(ICacheRetrieverExtendable.class)
						.with(Object.class);
				// beanContextFactory.RegisterAlias(CacheModule.ROOT_CACHE_RETRIEVER,
				// CacheModule.EXTERNAL_CACHE_SERVICE);
				// beanContextFactory.registerBean<CacheServiceDelegate>("cacheService").autowireable<ICacheService>();
			}
		}

		beanContextFactory.registerBean(DataObjectMixin.class).autowireable(DataObjectMixin.class);
		beanContextFactory.registerBean(EntityEqualsMixin.class).autowireable(EntityEqualsMixin.class);
		beanContextFactory.registerBean(EmbeddedTypeMixin.class).autowireable(EmbeddedTypeMixin.class);
		beanContextFactory.registerBean(PropertyChangeMixin.class).autowireable(
				PropertyChangeMixin.class, IPropertyChangeExtensionExtendable.class,
				ICollectionChangeExtensionExtendable.class, IPropertyChangeItemListenerExtendable.class);
		beanContextFactory.registerBean(ValueHolderContainerMixin.class)
				.autowireable(ValueHolderContainerMixin.class, IAsyncLazyLoadController.class);
	}
}
