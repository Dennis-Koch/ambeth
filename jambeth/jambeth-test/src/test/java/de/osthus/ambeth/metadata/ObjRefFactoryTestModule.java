package de.osthus.ambeth.metadata;

import de.osthus.ambeth.cache.valueholdercontainer.CacheRetrieverMock;
import de.osthus.ambeth.cache.valueholdercontainer.Material;
import de.osthus.ambeth.cache.valueholdercontainer.MaterialType;
import de.osthus.ambeth.ioc.CacheModule;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.MergeModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.service.ICacheRetrieverExtendable;

public class ObjRefFactoryTestModule implements IInitializingModule
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