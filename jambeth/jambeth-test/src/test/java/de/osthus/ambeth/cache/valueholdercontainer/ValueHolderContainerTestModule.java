package de.osthus.ambeth.cache.valueholdercontainer;

import de.osthus.ambeth.ioc.CacheModule;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.MergeModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.service.ICacheRetriever;
import de.osthus.ambeth.service.ICacheService;

public class ValueHolderContainerTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory)
	{
		IBeanConfiguration cacheRetrieverMockBC = beanContextFactory.registerBean(CacheModule.EXTERNAL_CACHE_SERVICE, CacheRetrieverMock.class).propertyRef(
				"reader", MergeModule.INDEPENDENT_META_DATA_READER);

		cacheRetrieverMockBC.autowireable(ICacheRetriever.class);
		cacheRetrieverMockBC.autowireable(ICacheService.class);

		beanContextFactory.registerAlias(CacheModule.ROOT_CACHE_RETRIEVER, CacheModule.EXTERNAL_CACHE_SERVICE);
	}
}