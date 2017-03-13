package com.koch.ambeth.cache.mock;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.IRevertChangesHelper;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.merge.cache.ICacheProvider;
import com.koch.ambeth.merge.util.IPrefetchHelper;

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
}
