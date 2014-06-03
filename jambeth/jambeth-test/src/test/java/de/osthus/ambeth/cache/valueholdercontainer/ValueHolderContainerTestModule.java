package de.osthus.ambeth.cache.valueholdercontainer;

import de.osthus.ambeth.event.EntityMetaDataAddedEvent;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.ioc.CacheModule;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.MergeModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.extendable.ExtendableBean;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.EntityMetaDataProvider;
import de.osthus.ambeth.merge.IEntityMetaDataExtendable;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IValueObjectConfigExtendable;
import de.osthus.ambeth.merge.ValueObjectMap;
import de.osthus.ambeth.merge.config.ValueObjectConfigReader;
import de.osthus.ambeth.merge.model.IEntityLifecycleExtendable;
import de.osthus.ambeth.orm.IOrmXmlReaderExtendable;
import de.osthus.ambeth.orm.IOrmXmlReaderRegistry;
import de.osthus.ambeth.orm.OrmXmlReader20;
import de.osthus.ambeth.orm.OrmXmlReaderLegathy;
import de.osthus.ambeth.service.ICacheService;
import de.osthus.ambeth.service.IMergeService;
import de.osthus.ambeth.typeinfo.IRelationProvider;
import de.osthus.ambeth.typeinfo.RelationProvider;
import de.osthus.ambeth.util.XmlConfigUtil;
import de.osthus.ambeth.util.xml.IXmlConfigUtil;

public class ValueHolderContainerTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory)
	{
		IBeanConfiguration cacheRetrieverMockBC = beanContextFactory.registerBean(CacheModule.EXTERNAL_CACHE_SERVICE, CacheRetrieverMock.class).propertyRef(
				"reader", MergeModule.INDEPENDENT_META_DATA_READER);

		cacheRetrieverMockBC.autowireable(IMergeService.class);
		// cacheRetrieverMockBC.autowireable(IClientServiceFactory.class);
		cacheRetrieverMockBC.autowireable(ICacheService.class);

		beanContextFactory.registerAlias(CacheModule.DEFAULT_CACHE_RETRIEVER, CacheModule.EXTERNAL_CACHE_SERVICE);
		beanContextFactory.registerAlias(CacheModule.ROOT_CACHE_RETRIEVER, CacheModule.EXTERNAL_CACHE_SERVICE);

		// beanContextFactory.Link(cacheRetrieverMockBC).To<ICacheRetrieverExtendable>().With(typeof(Material));

		IBeanConfiguration valueObjectMap = beanContextFactory.registerAnonymousBean(ValueObjectMap.class);
		beanContextFactory
				.registerBean("independantMetaDataProvider", EntityMetaDataProvider.class)
				.propertyRef("ValueObjectMap", valueObjectMap)
				.autowireable(IEntityMetaDataProvider.class, IValueObjectConfigExtendable.class, IEntityLifecycleExtendable.class,
						IEntityMetaDataExtendable.class, EntityMetaDataProvider.class);
		beanContextFactory.registerBean("valueObjectConfigReader", ValueObjectConfigReader.class);
		beanContextFactory.link("valueObjectConfigReader").to(IEventListenerExtendable.class).with(EntityMetaDataAddedEvent.class);

		beanContextFactory.registerBean("ormXmlReader", ExtendableBean.class).propertyValue(ExtendableBean.P_PROVIDER_TYPE, IOrmXmlReaderRegistry.class)
				.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, IOrmXmlReaderExtendable.class)
				.propertyRef(ExtendableBean.P_DEFAULT_BEAN, "ormXmlReaderLegathy").autowireable(IOrmXmlReaderRegistry.class, IOrmXmlReaderExtendable.class);
		beanContextFactory.registerBean("ormXmlReaderLegathy", OrmXmlReaderLegathy.class);
		beanContextFactory.registerBean("ormXmlReader 2.0", OrmXmlReader20.class);
		beanContextFactory.link("ormXmlReader 2.0").to(IOrmXmlReaderExtendable.class).with(OrmXmlReader20.ORM_XML_NS);

		beanContextFactory.registerBean("xmlConfigUtil", XmlConfigUtil.class).autowireable(IXmlConfigUtil.class);
		beanContextFactory.registerBean("relationProvider", RelationProvider.class).autowireable(IRelationProvider.class);
	}
}