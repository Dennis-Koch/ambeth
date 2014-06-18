package de.osthus.ambeth.cache;

import java.util.Date;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.EntityMetaDataFake;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.service.ICacheRetriever;
import de.osthus.ambeth.service.ICacheRetrieverExtendable;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.typeinfo.MethodPropertyInfo;
import de.osthus.ambeth.typeinfo.PropertyInfoItem;

public class CacheRetrieverTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		EntityMetaDataFake entityMetaDataProvider = new EntityMetaDataFake();
		entityMetaDataProvider.addMetaData(Date.class, new PropertyInfoItem(new MethodPropertyInfo(Date.class, "Time", Date.class.getMethod("getTime"),
				Date.class.getMethod("setTime", long.class))), null, new ITypeInfoItem[0], new IRelationInfoItem[0]);

		beanContextFactory.registerWithLifecycle(entityMetaDataProvider).autowireable(IEntityMetaDataProvider.class);

		beanContextFactory.registerBean("cacheRetrieverRegistry", CacheRetrieverRegistry.class).propertyRef("DefaultCacheRetriever", "cr1")
				.autowireable(ICacheRetriever.class, ICacheRetrieverExtendable.class, CacheRetrieverRegistry.class);

		CacheRetrieverFake cr1 = new CacheRetrieverFake();
		CacheRetrieverFake cr2 = new CacheRetrieverFake();

		for (int i = 0; i < 2; i++)
		{
			cr1.entities.put(CacheRetrieverRegistryTest.objRefs[i], new LoadContainerFake(CacheRetrieverRegistryTest.objRefs[i], null, null));
		}
		for (int i = 2; i < 4; i++)
		{
			cr2.entities.put(CacheRetrieverRegistryTest.objRefs[i], new LoadContainerFake(CacheRetrieverRegistryTest.objRefs[i], null, null));
		}

		beanContextFactory.registerWithLifecycle("cr1", cr1);
		beanContextFactory.registerWithLifecycle("cr2", cr2);

		beanContextFactory.link("cr2").to(ICacheRetrieverExtendable.class).with(Integer.class);
		beanContextFactory.link("cr2").to(ICacheRetrieverExtendable.class).with(Date.class);
	}
}