package de.osthus.ambeth.bytecode;

import de.osthus.ambeth.bytecode.abstractobject.IImplementAbstractObjectFactory;
import de.osthus.ambeth.bytecode.abstractobject.IImplementAbstractObjectFactoryExtendable;
import de.osthus.ambeth.bytecode.abstractobject.ImplementAbstractObjectFactory;
import de.osthus.ambeth.bytecode.behavior.ImplementAbstractObjectBehavior;
import de.osthus.ambeth.cache.mock.CacheMockModule;
import de.osthus.ambeth.ioc.BytecodeModule;
import de.osthus.ambeth.ioc.CompositeIdModule;
import de.osthus.ambeth.ioc.EventModule;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.MergeModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.extendable.ExtendableBean;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.DefaultProxyHelper;
import de.osthus.ambeth.merge.IEntityMetaDataExtendable;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.merge.IValueObjectConfigExtendable;
import de.osthus.ambeth.merge.IndependentEntityMetaDataClient;
import de.osthus.ambeth.merge.ValueObjectMap;
import de.osthus.ambeth.merge.config.IndependentEntityMetaDataReader;
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

public class PublicConstructorVisitorTestModule implements IInitializingModule
{
	private static final String IMPLEMENT_ABSTRACT_OBJECT_FACTORY = "implementAbstractObjectFactory";

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAnonymousBean(BytecodeModule.class);
		beanContextFactory.registerAnonymousBean(CompositeIdModule.class);
		beanContextFactory.registerAnonymousBean(CacheMockModule.class);
		beanContextFactory.registerAnonymousBean(MergeModule.class);
		beanContextFactory.registerAnonymousBean(EventModule.class);

		// creates objects that implement the interfaces
		beanContextFactory.registerBean(IMPLEMENT_ABSTRACT_OBJECT_FACTORY, ImplementAbstractObjectFactory.class).autowireable(
				IImplementAbstractObjectFactory.class, IImplementAbstractObjectFactoryExtendable.class);

		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, ImplementAbstractObjectBehavior.class).propertyRef("ImplementAbstractObjectFactory",
				IMPLEMENT_ABSTRACT_OBJECT_FACTORY);

		beanContextFactory.registerBean("proxyHelper", DefaultProxyHelper.class).autowireable(IProxyHelper.class);
		beanContextFactory.registerBean("relationProvider", RelationProvider.class).autowireable(IRelationProvider.class);
		beanContextFactory.registerBean("typeInfoProvider", TypeInfoProvider.class).autowireable(ITypeInfoProvider.class);
		beanContextFactory.registerBean("xmlConfigUtil", XmlConfigUtil.class).autowireable(IXmlConfigUtil.class);

		IBeanConfiguration valueObjectMap = beanContextFactory.registerAnonymousBean(ValueObjectMap.class);
		beanContextFactory
				.registerBean("independantMetaDataProvider", IndependentEntityMetaDataClient.class)
				.propertyRef("ValueObjectMap", valueObjectMap)
				.autowireable(IEntityMetaDataProvider.class, IValueObjectConfigExtendable.class, IEntityMetaDataExtendable.class,
						IndependentEntityMetaDataClient.class);
		beanContextFactory.registerBean(MergeModule.INDEPENDENT_META_DATA_READER, IndependentEntityMetaDataReader.class);

		beanContextFactory.registerBean("xmlTypeHelper", XmlTypeHelper.class).autowireable(IXmlTypeHelper.class);

		beanContextFactory.registerBean("ormXmlReader", ExtendableBean.class).propertyValue(ExtendableBean.P_PROVIDER_TYPE, IOrmXmlReaderRegistry.class)
				.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, IOrmXmlReaderExtendable.class)
				.propertyRef(ExtendableBean.P_DEFAULT_BEAN, "ormXmlReaderLegathy").autowireable(IOrmXmlReaderRegistry.class, IOrmXmlReaderExtendable.class);
		beanContextFactory.registerBean("ormXmlReaderLegathy", OrmXmlReaderLegathy.class);
		beanContextFactory.registerBean("ormXmlReader 2.0", OrmXmlReader20.class);
		beanContextFactory.link("ormXmlReader 2.0").to(IOrmXmlReaderExtendable.class).with(OrmXmlReader20.ORM_XML_NS);
	}
};
