package com.koch.ambeth.cache.valueholdercontainer;

import com.koch.ambeth.cache.ioc.CacheModule;
import com.koch.ambeth.cache.service.ICacheRetrieverExtendable;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.ioc.MergeModule;

public class ValueHolderContainerTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory)
	{
		IBeanConfiguration cacheRetrieverMockBC = beanContextFactory.registerBean(CacheModule.EXTERNAL_CACHE_SERVICE, CacheRetrieverMock.class).propertyRef(
				"reader", MergeModule.INDEPENDENT_META_DATA_READER);

		beanContextFactory.link(cacheRetrieverMockBC).to(ICacheRetrieverExtendable.class).with(Material.class);
		beanContextFactory.link(cacheRetrieverMockBC).to(ICacheRetrieverExtendable.class).with(MaterialType.class);
	}
}