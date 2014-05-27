package de.osthus.ambeth.ioc;

import net.sf.cglib.proxy.MethodInterceptor;
import de.osthus.ambeth.cache.CacheEventTargetExtractor;
import de.osthus.ambeth.cache.CacheFactory;
import de.osthus.ambeth.cache.CacheProvider;
import de.osthus.ambeth.cache.CacheRetrieverRegistry;
import de.osthus.ambeth.cache.CacheType;
import de.osthus.ambeth.cache.FirstLevelCacheManager;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheContext;
import de.osthus.ambeth.cache.ICacheFactory;
import de.osthus.ambeth.cache.ICacheIntern;
import de.osthus.ambeth.cache.ICacheProvider;
import de.osthus.ambeth.cache.ICacheProviderExtendable;
import de.osthus.ambeth.cache.IFirstLevelCacheExtendable;
import de.osthus.ambeth.cache.IFirstLevelCacheManager;
import de.osthus.ambeth.cache.IRootCache;
import de.osthus.ambeth.cache.ISecondLevelCacheManager;
import de.osthus.ambeth.cache.IServiceResultCache;
import de.osthus.ambeth.cache.IServiceResultProcessorExtendable;
import de.osthus.ambeth.cache.ITransactionalRootCache;
import de.osthus.ambeth.cache.IWritableCache;
import de.osthus.ambeth.cache.PagingQueryServiceResultProcessor;
import de.osthus.ambeth.cache.RootCache;
import de.osthus.ambeth.cache.RootCacheBridge;
import de.osthus.ambeth.cache.ServiceResultCache;
import de.osthus.ambeth.cache.ValueHolderIEC;
import de.osthus.ambeth.cache.collections.CacheMapEntryTypeProvider;
import de.osthus.ambeth.cache.collections.ICacheMapEntryTypeProvider;
import de.osthus.ambeth.cache.config.CacheConfigurationConstants;
import de.osthus.ambeth.cache.config.CacheNamedBeans;
import de.osthus.ambeth.cache.interceptor.CacheProviderInterceptor;
import de.osthus.ambeth.cache.interceptor.ThreadLocalRootCacheInterceptor;
import de.osthus.ambeth.cache.interceptor.TransactionalRootCacheInterceptor;
import de.osthus.ambeth.cache.rootcachevalue.IRootCacheValueTypeProvider;
import de.osthus.ambeth.cache.rootcachevalue.RootCacheValueTypeProvider;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.event.IEventTargetExtractorExtendable;
import de.osthus.ambeth.filter.model.IPagingResponse;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.merge.config.MergeConfigurationConstants;
import de.osthus.ambeth.proxy.CacheContextPostProcessor;
import de.osthus.ambeth.proxy.CacheEntityFactory;
import de.osthus.ambeth.proxy.CachePostProcessor;
import de.osthus.ambeth.proxy.IProxyFactory;
import de.osthus.ambeth.service.ICacheRetrieverExtendable;
import de.osthus.ambeth.service.ICacheServiceByNameExtendable;
import de.osthus.ambeth.service.IOfflineListener;
import de.osthus.ambeth.service.IOfflineListenerExtendable;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.util.CacheHelper;
import de.osthus.ambeth.util.ICacheHelper;
import de.osthus.ambeth.util.ICachePathHelper;
import de.osthus.ambeth.util.IPrefetchHelper;
import de.osthus.ambeth.util.ParamChecker;

@FrameworkModule
public class CacheModule implements IInitializingModule, IPropertyLoadingBean
{
	public static final String CACHE_DATA_CHANGE_LISTENER = "cache.dcl";

	public static final String COMMITTED_ROOT_CACHE = "committedRootCache";

	public static final String INTERNAL_CACHE_SERVICE = "cacheService.internal";

	public static final String EXTERNAL_CACHE_SERVICE = "cacheService.external";

	public static final String DEFAULT_CACHE_RETRIEVER = "cacheRetriever.default";

	public static final String ROOT_CACHE_RETRIEVER = "cacheRetriever.rootCache";

	public static final String ROOT_CACHE = "rootCache";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = ConfigurationConstants.NetworkClientMode, defaultValue = "false")
	protected boolean isNetworkClientMode;

	@Property(name = ConfigurationConstants.GenericTransferMapping, defaultValue = "false")
	protected boolean genericTransferMapping;

	@Autowired
	protected IProxyFactory proxyFactory;

	@Property(name = CacheConfigurationConstants.FirstLevelCacheType, mandatory = false)
	protected CacheType defaultCacheType;

	@Property(name = CacheConfigurationConstants.SecondLevelCacheActive, defaultValue = "true")
	protected boolean secondLevelCacheActive;

	@Property(name = CacheConfigurationConstants.CacheServiceRegistryActive, defaultValue = "true")
	protected boolean cacheServiceRegistryActive;

	@Override
	public void applyProperties(Properties contextProperties)
	{
		contextProperties.putString(MergeConfigurationConstants.EntityFactoryType, CacheEntityFactory.class.getName());
	}

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		if (defaultCacheType == null)
		{
			defaultCacheType = CacheType.DEFAULT;
		}
		ParamChecker.assertNotNull(proxyFactory, "proxyFactory");

		beanContextFactory.registerBean("serviceResultCache", ServiceResultCache.class).autowireable(IServiceResultCache.class);

		beanContextFactory.registerBean("proxyHelper", ValueHolderIEC.class).autowireable(ValueHolderIEC.class, IProxyHelper.class);

		beanContextFactory.registerAnonymousBean(CacheHelper.class).autowireable(ICacheHelper.class, ICachePathHelper.class, IPrefetchHelper.class);

		beanContextFactory.registerAutowireableBean(ICacheMapEntryTypeProvider.class, CacheMapEntryTypeProvider.class);

		beanContextFactory.registerAutowireableBean(IRootCacheValueTypeProvider.class, RootCacheValueTypeProvider.class);

		if (cacheServiceRegistryActive)
		{
			beanContextFactory.registerBean("cacheRetrieverRegistry", CacheRetrieverRegistry.class)
					.propertyRef("DefaultCacheRetriever", DEFAULT_CACHE_RETRIEVER)
					.autowireable(ICacheServiceByNameExtendable.class, ICacheRetrieverExtendable.class);
			beanContextFactory.registerAlias(ROOT_CACHE_RETRIEVER, "cacheRetrieverRegistry");
		}

		beanContextFactory.registerBean("firstLevelCacheManager", FirstLevelCacheManager.class).autowireable(IFirstLevelCacheExtendable.class,
				IFirstLevelCacheManager.class);

		String rootCacheBridge = "rootCacheBridge";

		beanContextFactory.registerBean(rootCacheBridge, RootCacheBridge.class).propertyRefs(COMMITTED_ROOT_CACHE, ROOT_CACHE_RETRIEVER);

		TransactionalRootCacheInterceptor txRcInterceptor = new TransactionalRootCacheInterceptor();

		beanContextFactory.registerWithLifecycle("txRootCacheInterceptor", txRcInterceptor).propertyRefs(COMMITTED_ROOT_CACHE, rootCacheBridge)
				.autowireable(ITransactionalRootCache.class, ISecondLevelCacheManager.class);

		Object txRcProxy = proxyFactory.createProxy(new Class<?>[] { IRootCache.class, ICacheIntern.class, IOfflineListener.class }, txRcInterceptor);

		beanContextFactory.registerExternalBean(ROOT_CACHE, txRcProxy).autowireable(IRootCache.class, ICacheIntern.class);

		if (secondLevelCacheActive)
		{
			// One single root cache instance for whole context
			beanContextFactory.registerBean(COMMITTED_ROOT_CACHE, RootCache.class).propertyRef("CacheRetriever", ROOT_CACHE_RETRIEVER);
			beanContextFactory.link(COMMITTED_ROOT_CACHE).to(IOfflineListenerExtendable.class);
		}
		else
		{
			// One root cache instance per thread sequence. Most often used in server environment where the "deactivated"
			// second level cache means that each thread hold his own, isolated root cache (which gets cleared with each service
			// request. Effectively this means that the root cache itself only lives per-request and does not hold a longer state
			ThreadLocalRootCacheInterceptor threadLocalRcInterceptor = new ThreadLocalRootCacheInterceptor();

			beanContextFactory.registerWithLifecycle("threadLocalRootCacheInterceptor", threadLocalRcInterceptor).propertyRef("StoredCacheRetriever",
					CacheModule.ROOT_CACHE_RETRIEVER);

			IRootCache threadLocalRcProxy = proxyFactory.createProxy(IRootCache.class, threadLocalRcInterceptor);

			beanContextFactory.registerExternalBean(COMMITTED_ROOT_CACHE, threadLocalRcProxy);
		}
		beanContextFactory.registerBean("cacheEventTargetExtractor", CacheEventTargetExtractor.class);
		beanContextFactory.link("cacheEventTargetExtractor").to(IEventTargetExtractorExtendable.class).with(ICache.class).optional();

		beanContextFactory.registerBean("cacheFactory", CacheFactory.class).autowireable(ICacheFactory.class);

		beanContextFactory.registerAlias(MergeModule.MERGE_CACHE_FACTORY, "cacheFactory");

		MethodInterceptor cacheProviderInterceptor = (MethodInterceptor) beanContextFactory
				.registerBean("cacheProviderInterceptor", CacheProviderInterceptor.class)
				.autowireable(ICacheProviderExtendable.class, ICacheProvider.class, ICacheContext.class).getInstance();

		Object cacheProxy = proxyFactory.createProxy(ICache.class, new Class[] { ICacheProvider.class, IWritableCache.class }, cacheProviderInterceptor);
		beanContextFactory.registerExternalBean("cache", cacheProxy).autowireable(ICache.class);

		beanContextFactory.registerBean("pagingQuerySRP", PagingQueryServiceResultProcessor.class);
		beanContextFactory.link("pagingQuerySRP").to(IServiceResultProcessorExtendable.class).with(IPagingResponse.class);

		beanContextFactory.registerBean(CacheNamedBeans.CacheProviderSingleton, CacheProvider.class).propertyValue("CacheType", CacheType.SINGLETON);

		beanContextFactory.registerBean(CacheNamedBeans.CacheProviderThreadLocal, CacheProvider.class).propertyValue("CacheType", CacheType.THREAD_LOCAL);

		beanContextFactory.registerBean(CacheNamedBeans.CacheProviderPrototype, CacheProvider.class).propertyValue("CacheType", CacheType.PROTOTYPE);

		String defaultCacheProviderBeanName;
		switch (defaultCacheType)
		{
			case PROTOTYPE:
			{
				defaultCacheProviderBeanName = CacheNamedBeans.CacheProviderPrototype;
				break;
			}
			case SINGLETON:
			{
				defaultCacheProviderBeanName = CacheNamedBeans.CacheProviderSingleton;
				break;
			}
			case DEFAULT:
			case THREAD_LOCAL:
			{
				defaultCacheProviderBeanName = CacheNamedBeans.CacheProviderThreadLocal;
				break;
			}
			default:
				throw new IllegalStateException("Unsupported " + CacheType.class.getName() + ": " + defaultCacheType);
		}
		beanContextFactory.link(defaultCacheProviderBeanName).to(ICacheProviderExtendable.class);

		// CacheContextPostProcessor must be registered AFTER CachePostProcessor...
		beanContextFactory.registerBean("cachePostProcessor", CachePostProcessor.class);
		beanContextFactory.registerBean("cacheContextPostProcessor", CacheContextPostProcessor.class);

		if (isNetworkClientMode)
		{

		}
		else
		{

		}
	}
}
