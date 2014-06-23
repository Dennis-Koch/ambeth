package de.osthus.ambeth.xml;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public class XmlTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		// beanContextFactory.registerBean("proxyHelper", DefaultProxyHelper.class).autowireable(IProxyHelper.class);
		//
		// beanContextFactory.registerBean("typeInfoProvider", TypeInfoProvider.class).autowireable(ITypeInfoProvider.class);
		//
		// beanContextFactory.registerBean("relationProvider", RelationProvider.class).autowireable(IRelationProvider.class);
		//
		// beanContextFactory.registerBean("entityMetaDataProviderDummy", EntityMetaDataProviderDummy.class).autowireable(IEntityMetaDataProvider.class);
		//
		// beanContextFactory.registerBean("cache", CacheDummy.class).autowireable(ICache.class);
		//
		// beanContextFactory.registerBean("oriHelper", OriHelperDummy.class).autowireable(IObjRefHelper.class);
		//
		// beanContextFactory.registerBean("entityFactory", EntityFactoryDummy.class).autowireable(IEntityFactory.class);
		//
		// beanContextFactory.registerBean("cacheFactory", CacheFactoryDummy.class).autowireable(ICacheFactory.class);
		//
		// beanContextFactory.registerBean("mergeController", MergeControllerDummy.class).autowireable(IMergeController.class);
		//
		// beanContextFactory.registerBean("prefetchHelper", PrefetchHelperDummy.class).autowireable(IPrefetchHelper.class);
	}
}
