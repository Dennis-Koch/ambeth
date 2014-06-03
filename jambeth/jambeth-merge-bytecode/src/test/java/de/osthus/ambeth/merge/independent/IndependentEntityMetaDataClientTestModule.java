package de.osthus.ambeth.merge.independent;

import de.osthus.ambeth.event.EntityMetaDataAddedEvent;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.MergeModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.extendable.ExtendableBean;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.DefaultProxyHelper;
import de.osthus.ambeth.merge.EntityMetaDataProvider;
import de.osthus.ambeth.merge.IEntityMetaDataExtendable;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.merge.IValueObjectConfigExtendable;
import de.osthus.ambeth.merge.ValueObjectMap;
import de.osthus.ambeth.merge.config.EntityMetaDataReader;
import de.osthus.ambeth.merge.config.IEntityMetaDataReader;
import de.osthus.ambeth.merge.config.IndependentEntityMetaDataReader;
import de.osthus.ambeth.merge.config.ValueObjectConfigReader;
import de.osthus.ambeth.merge.model.IEntityLifecycleExtendable;
import de.osthus.ambeth.orm.IOrmXmlReaderExtendable;
import de.osthus.ambeth.orm.IOrmXmlReaderRegistry;
import de.osthus.ambeth.orm.OrmXmlReader20;
import de.osthus.ambeth.orm.OrmXmlReaderLegathy;
import de.osthus.ambeth.typeinfo.IRelationProvider;
import de.osthus.ambeth.typeinfo.ITypeInfoProvider;
import de.osthus.ambeth.typeinfo.RelationProvider;
import de.osthus.ambeth.typeinfo.TypeInfoProvider;
import de.osthus.ambeth.util.XmlConfigUtil;
import de.osthus.ambeth.util.xml.IXmlConfigUtil;
import de.osthus.ambeth.xml.IXmlTypeHelper;
import de.osthus.ambeth.xml.XmlTypeHelper;

public class IndependentEntityMetaDataClientTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("proxyHelper", DefaultProxyHelper.class).autowireable(IProxyHelper.class);
		beanContextFactory.registerBean("relationProvider", RelationProvider.class).autowireable(IRelationProvider.class);
		beanContextFactory.registerBean("typeInfoProvider", TypeInfoProvider.class).autowireable(ITypeInfoProvider.class);
		beanContextFactory.registerBean("xmlConfigUtil", XmlConfigUtil.class).autowireable(IXmlConfigUtil.class);

		IBeanConfiguration valueObjectMap = beanContextFactory.registerAnonymousBean(ValueObjectMap.class);
		beanContextFactory
				.registerBean("independantMetaDataProvider", EntityMetaDataProvider.class)
				.propertyRef("ValueObjectMap", valueObjectMap)
				.autowireable(IEntityMetaDataProvider.class, IValueObjectConfigExtendable.class, IEntityLifecycleExtendable.class,
						IEntityMetaDataExtendable.class, EntityMetaDataProvider.class);
		beanContextFactory.registerBean("entityMetaDataReader", EntityMetaDataReader.class).autowireable(IEntityMetaDataReader.class);
		beanContextFactory.registerBean(MergeModule.INDEPENDENT_META_DATA_READER, IndependentEntityMetaDataReader.class);
		beanContextFactory.registerBean("valueObjectConfigReader", ValueObjectConfigReader.class);
		beanContextFactory.link("valueObjectConfigReader").to(IEventListenerExtendable.class).with(EntityMetaDataAddedEvent.class);

		beanContextFactory.registerBean("xmlTypeHelper", XmlTypeHelper.class).autowireable(IXmlTypeHelper.class);

		beanContextFactory.registerBean("ormXmlReader", ExtendableBean.class).propertyValue(ExtendableBean.P_PROVIDER_TYPE, IOrmXmlReaderRegistry.class)
				.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, IOrmXmlReaderExtendable.class)
				.propertyRef(ExtendableBean.P_DEFAULT_BEAN, "ormXmlReaderLegathy").autowireable(IOrmXmlReaderRegistry.class, IOrmXmlReaderExtendable.class);
		beanContextFactory.registerBean("ormXmlReaderLegathy", OrmXmlReaderLegathy.class);
		beanContextFactory.registerBean("ormXmlReader 2.0", OrmXmlReader20.class);
		beanContextFactory.link("ormXmlReader 2.0").to(IOrmXmlReaderExtendable.class).with(OrmXmlReader20.ORM_XML_NS);
	}
};
