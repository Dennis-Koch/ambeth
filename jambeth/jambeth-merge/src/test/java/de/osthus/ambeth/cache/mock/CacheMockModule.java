package de.osthus.ambeth.cache.mock;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheFactory;
import de.osthus.ambeth.cache.ICacheProvider;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.IRevertChangesHelper;
import de.osthus.ambeth.util.IPrefetchHelper;

/**
 * Support for unit tests that do not include jAmbeth.Cache
 */
public class CacheMockModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("revertChangesHelper", RevertChangesHelperMock.class).autowireable(IRevertChangesHelper.class);
		beanContextFactory.registerBean("cache", CacheMock.class).autowireable(ICache.class);
		beanContextFactory.registerBean("cacheFactory", CacheFactoryMock.class).autowireable(ICacheFactory.class);
		beanContextFactory.registerBean("cacheProvider", CacheProviderMock.class).autowireable(ICacheProvider.class);
		beanContextFactory.registerBean("prefetchHelper", PrefetchHelperMock.class).autowireable(IPrefetchHelper.class);
	}
};
