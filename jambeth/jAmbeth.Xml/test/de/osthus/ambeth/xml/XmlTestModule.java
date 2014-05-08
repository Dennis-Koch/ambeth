package de.osthus.ambeth.xml;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheFactory;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.DefaultProxyHelper;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IMergeController;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.typeinfo.IRelationProvider;
import de.osthus.ambeth.typeinfo.ITypeInfoProvider;
import de.osthus.ambeth.typeinfo.RelationProvider;
import de.osthus.ambeth.typeinfo.TypeInfoProvider;
import de.osthus.ambeth.util.IPrefetchHelper;

public class XmlTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("proxyHelper", DefaultProxyHelper.class).autowireable(IProxyHelper.class);

		beanContextFactory.registerBean("typeInfoProvider", TypeInfoProvider.class).autowireable(ITypeInfoProvider.class);

		beanContextFactory.registerBean("relationProvider", RelationProvider.class).autowireable(IRelationProvider.class);

		beanContextFactory.registerBean("entityMetaDataProviderDummy", EntityMetaDataProviderDummy.class).autowireable(IEntityMetaDataProvider.class);

		beanContextFactory.registerBean("cache", CacheDummy.class).autowireable(ICache.class);

		beanContextFactory.registerBean("oriHelper", OriHelperDummy.class).autowireable(IObjRefHelper.class);

		beanContextFactory.registerBean("entityFactory", EntityFactoryDummy.class).autowireable(IEntityFactory.class);

		beanContextFactory.registerBean("cacheFactory", CacheFactoryDummy.class).autowireable(ICacheFactory.class);

		beanContextFactory.registerBean("mergeController", MergeControllerDummy.class).autowireable(IMergeController.class);

		beanContextFactory.registerBean("prefetchHelper", PrefetchHelperDummy.class).autowireable(IPrefetchHelper.class);
	}
}
