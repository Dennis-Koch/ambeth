package de.osthus.ambeth.ioc;

import de.osthus.ambeth.cache.CacheModification;
import de.osthus.ambeth.cache.ICacheModification;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.copy.IObjectCopierExtendable;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.config.PrecedenceType;
import de.osthus.ambeth.ioc.extendable.ExtendableBean;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.CUDResultHelper;
import de.osthus.ambeth.merge.EntityMetaDataProvider;
import de.osthus.ambeth.merge.ICUDResultExtendable;
import de.osthus.ambeth.merge.ICUDResultHelper;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IEntityInstantiationExtensionExtendable;
import de.osthus.ambeth.merge.IEntityMetaDataExtendable;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IEntityMetaDataRefresher;
import de.osthus.ambeth.merge.IMergeController;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.merge.IMergeServiceExtensionExtendable;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.ITechnicalEntityTypeExtendable;
import de.osthus.ambeth.merge.IValueObjectConfigExtendable;
import de.osthus.ambeth.merge.MergeController;
import de.osthus.ambeth.merge.MergeProcess;
import de.osthus.ambeth.merge.MergeServiceRegistry;
import de.osthus.ambeth.merge.ObjRefHelper;
import de.osthus.ambeth.merge.ValueObjectMap;
import de.osthus.ambeth.merge.config.EntityMetaDataReader;
import de.osthus.ambeth.merge.config.IEntityMetaDataReader;
import de.osthus.ambeth.merge.config.IndependentEntityMetaDataReader;
import de.osthus.ambeth.merge.config.MergeConfigurationConstants;
import de.osthus.ambeth.merge.converter.EntityMetaDataConverter;
import de.osthus.ambeth.merge.model.EntityMetaData;
import de.osthus.ambeth.merge.model.IEntityLifecycleExtendable;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.EntityMetaDataTransfer;
import de.osthus.ambeth.metadata.IIntermediateMemberTypeProvider;
import de.osthus.ambeth.metadata.IMemberTypeProvider;
import de.osthus.ambeth.metadata.IObjRefFactory;
import de.osthus.ambeth.metadata.MemberTypeProvider;
import de.osthus.ambeth.metadata.ObjRefFactory;
import de.osthus.ambeth.metadata.ObjRefObjectCopierExtension;
import de.osthus.ambeth.objrefstore.IObjRefStoreEntryProvider;
import de.osthus.ambeth.objrefstore.ObjRefStoreEntryProvider;
import de.osthus.ambeth.orm.IOrmXmlReaderExtendable;
import de.osthus.ambeth.orm.IOrmXmlReaderRegistry;
import de.osthus.ambeth.orm.OrmXmlReader20;
import de.osthus.ambeth.orm.OrmXmlReaderLegathy;
import de.osthus.ambeth.proxy.EntityFactory;
import de.osthus.ambeth.service.IMergeService;
import de.osthus.ambeth.template.CompositeIdTemplate;
import de.osthus.ambeth.template.EmbeddedMemberTemplate;
import de.osthus.ambeth.template.ObjRefTemplate;
import de.osthus.ambeth.typeinfo.IRelationProvider;
import de.osthus.ambeth.typeinfo.RelationProvider;
import de.osthus.ambeth.util.DedicatedConverterUtil;
import de.osthus.ambeth.util.XmlConfigUtil;
import de.osthus.ambeth.util.xml.IXmlConfigUtil;

@FrameworkModule
public class MergeModule implements IInitializingModule
{
	public static final String INDEPENDENT_META_DATA_READER = "independentEntityMetaDataReader";

	public static final String REMOTE_ENTITY_METADATA_PROVIDER = "entityMetaDataProvider.remote";

	@Property(name = ServiceConfigurationConstants.IndependentMetaData, defaultValue = "false")
	protected boolean independentMetaData;

	@Property(name = MergeConfigurationConstants.EntityFactoryType, mandatory = false)
	protected Class<?> entityFactoryType;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAutowireableBean(IMergeController.class, MergeController.class);
		beanContextFactory.registerAutowireableBean(IMergeProcess.class, MergeProcess.class);

		beanContextFactory.registerAutowireableBean(CompositeIdTemplate.class, CompositeIdTemplate.class);
		beanContextFactory.registerAutowireableBean(ObjRefTemplate.class, ObjRefTemplate.class);

		// if (isNetworkClientMode)
		// {
		// metaDataClient =
		// serviceExtendable.registerObject(EntityMetaDataClient.class, new
		// Object[0]);
		//
		// EntityMetaDataCache metaDataCache =
		// serviceExtendable.registerService(IEntityMetaDataProvider.class,
		// EntityMetaDataCache.class);
		// metaDataCache.setEntityMetaDataProvider(metaDataClient);
		// }
		beanContextFactory.registerAnonymousBean(CacheModification.class).autowireable(ICacheModification.class);

		beanContextFactory.registerAutowireableBean(IObjRefHelper.class, ObjRefHelper.class);
		beanContextFactory.registerAnonymousBean(CUDResultHelper.class).autowireable(ICUDResultHelper.class, ICUDResultExtendable.class);

		beanContextFactory.registerAnonymousBean(EntityMetaDataReader.class).autowireable(IEntityMetaDataReader.class);

		beanContextFactory.registerAnonymousBean(MergeServiceRegistry.class).autowireable(IMergeService.class, IMergeServiceExtensionExtendable.class);

		IBeanConfiguration valueObjectMap = beanContextFactory.registerAnonymousBean(ValueObjectMap.class);
		beanContextFactory
				.registerAnonymousBean(EntityMetaDataProvider.class)
				.propertyRef("ValueObjectMap", valueObjectMap)
				.autowireable(IEntityMetaDataProvider.class, IEntityMetaDataRefresher.class, IValueObjectConfigExtendable.class,
						IEntityLifecycleExtendable.class, ITechnicalEntityTypeExtendable.class, IEntityMetaDataExtendable.class, EntityMetaDataProvider.class,
						IEntityInstantiationExtensionExtendable.class);
		beanContextFactory.registerBean(INDEPENDENT_META_DATA_READER, IndependentEntityMetaDataReader.class).precedence(PrecedenceType.HIGH);

		if (!independentMetaData)
		{
			IBeanConfiguration entityMetaDataConverter = beanContextFactory.registerAnonymousBean(EntityMetaDataConverter.class);
			DedicatedConverterUtil.biLink(beanContextFactory, entityMetaDataConverter, EntityMetaData.class, EntityMetaDataTransfer.class);
		}
		else
		{
			// beanContextFactory.registerBean("valueObjectConfigReader", ValueObjectConfigReader.class);
			// beanContextFactory.link("valueObjectConfigReader").to(IEventListenerExtendable.class).with(EntityMetaDataAddedEvent.class);

			IBeanConfiguration ormXmlReaderLegathy = beanContextFactory.registerAnonymousBean(OrmXmlReaderLegathy.class);
			ExtendableBean.registerExtendableBean(beanContextFactory, IOrmXmlReaderRegistry.class, IOrmXmlReaderExtendable.class).propertyRef(
					ExtendableBean.P_DEFAULT_BEAN, ormXmlReaderLegathy);
			IBeanConfiguration ormXmlReader20BC = beanContextFactory.registerAnonymousBean(OrmXmlReader20.class);
			beanContextFactory.link(ormXmlReader20BC).to(IOrmXmlReaderExtendable.class).with(OrmXmlReader20.ORM_XML_NS);

			beanContextFactory.registerAnonymousBean(XmlConfigUtil.class).autowireable(IXmlConfigUtil.class);
		}
		beanContextFactory.registerAnonymousBean(RelationProvider.class).autowireable(IRelationProvider.class);

		beanContextFactory.registerAnonymousBean(MemberTypeProvider.class).autowireable(IMemberTypeProvider.class, IIntermediateMemberTypeProvider.class);
		beanContextFactory.registerAnonymousBean(EmbeddedMemberTemplate.class).autowireable(EmbeddedMemberTemplate.class);

		beanContextFactory.registerAnonymousBean(ObjRefFactory.class).autowireable(IObjRefFactory.class);
		IBeanConfiguration objRefObjectCopierExtension = beanContextFactory.registerAnonymousBean(ObjRefObjectCopierExtension.class);
		beanContextFactory.link(objRefObjectCopierExtension).to(IObjectCopierExtendable.class).with(IObjRef.class);

		Class<?> entityFactoryType = this.entityFactoryType;
		if (entityFactoryType == null)
		{
			entityFactoryType = EntityFactory.class;
		}
		beanContextFactory.registerBean("entityFactory", entityFactoryType).autowireable(IEntityFactory.class);

		beanContextFactory.registerAnonymousBean(ObjRefStoreEntryProvider.class).autowireable(IObjRefStoreEntryProvider.class);

		// if (isNetworkClientMode)
		// {
		// if (!independentMetaData)
		// {
		// beanContextFactory.registerBean("entityMetaDataClient", EntityMetaDataClient.class);
		//
		// IBeanConfiguration beanConfig = beanContextFactory.registerBean("entityMetaDataCache", EntityMetaDataCache.class)
		// .propertyRefs("entityMetaDataClient").autowireable(IEntityMetaDataProvider.class);
		// if (genericTransferMapping)
		// {
		// beanConfig.autowireable(IEntityMetaDataProviderExtendable.class);
		// }
		// }
		// else
		// {
		// beanContextFactory.registerBean("independentMetaDataProvider", EntityMetaDataProvider.class).autowireable(
		// IEntityMetaDataProvider.class, IEntityMetaDataProviderExtendable.class);
		// beanContextFactory.registerBean("entityMetaDataReader", EntityMetaDataReader.class);
		// }
		// }

		// serviceExtendable.registerObject(MergePostProcessor.class);

		// ITargetProvider targetProvider =
		// ServiceExtendable.RegisterObject<TypeInfoProviderTP>();

		// TargetingInterceptor tlInterceptor =
		// ServiceExtendable.RegisterObject<TargetingInterceptor>(targetProvider);

		// ServiceExtendable.RegisterService<ITypeInfoProvider>(ProxyFactory.CreateProxy<ITypeInfoProvider>(tlInterceptor));
	}

	// @Override
	// public void afterStarted(IServiceProvider serviceProvider)
	// {
	// // if (metaDataClient != null)
	// // {
	// //
	// metaDataClient.setMergeService(serviceProvider.getService(IServiceFactory.class).getService(
	// // IMergeService.class));
	// // }
	// }
}
