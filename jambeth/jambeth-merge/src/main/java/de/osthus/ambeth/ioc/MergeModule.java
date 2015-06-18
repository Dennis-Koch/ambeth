package de.osthus.ambeth.ioc;

import de.osthus.ambeth.cache.CacheModification;
import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.cache.ICacheModification;
import de.osthus.ambeth.changecontroller.ChangeController;
import de.osthus.ambeth.changecontroller.IChangeController;
import de.osthus.ambeth.changecontroller.IChangeControllerExtendable;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.copy.IObjectCopierExtendable;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.config.PrecedenceType;
import de.osthus.ambeth.ioc.extendable.ExtendableBean;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.CUDResultApplier;
import de.osthus.ambeth.merge.CUDResultComparer;
import de.osthus.ambeth.merge.CUDResultHelper;
import de.osthus.ambeth.merge.EntityMetaDataClient;
import de.osthus.ambeth.merge.EntityMetaDataProvider;
import de.osthus.ambeth.merge.ICUDResultApplier;
import de.osthus.ambeth.merge.ICUDResultComparer;
import de.osthus.ambeth.merge.ICUDResultExtendable;
import de.osthus.ambeth.merge.ICUDResultHelper;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IEntityInstantiationExtensionExtendable;
import de.osthus.ambeth.merge.IEntityMetaDataExtendable;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IEntityMetaDataRefresher;
import de.osthus.ambeth.merge.IMergeController;
import de.osthus.ambeth.merge.IMergeListenerExtendable;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.merge.IMergeServiceExtensionExtendable;
import de.osthus.ambeth.merge.IMergeTimeProvider;
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
import de.osthus.ambeth.mixin.CompositeIdMixin;
import de.osthus.ambeth.mixin.EmbeddedMemberMixin;
import de.osthus.ambeth.mixin.ObjRefMixin;
import de.osthus.ambeth.objrefstore.IObjRefStoreEntryProvider;
import de.osthus.ambeth.objrefstore.ObjRefStoreEntryProvider;
import de.osthus.ambeth.orm.IOrmConfigGroupProvider;
import de.osthus.ambeth.orm.IOrmXmlReaderExtendable;
import de.osthus.ambeth.orm.IOrmXmlReaderRegistry;
import de.osthus.ambeth.orm.OrmConfigGroupProvider;
import de.osthus.ambeth.orm.OrmXmlReader20;
import de.osthus.ambeth.orm.OrmXmlReaderLegathy;
import de.osthus.ambeth.proxy.EntityFactory;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.security.ISecurityScopeChangeListenerExtendable;
import de.osthus.ambeth.security.ISecurityScopeProvider;
import de.osthus.ambeth.security.SecurityActivation;
import de.osthus.ambeth.security.SecurityScopeProvider;
import de.osthus.ambeth.service.IMergeService;
import de.osthus.ambeth.typeinfo.INoEntityTypeExtendable;
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

	@Property(name = MergeConfigurationConstants.ChangeControllerType, mandatory = false)
	protected Class<?> changeControllerClass;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAutowireableBean(IMergeController.class, MergeController.class);
		beanContextFactory.registerAutowireableBean(IMergeProcess.class, MergeProcess.class);
		beanContextFactory.registerAutowireableBean(ICUDResultApplier.class, CUDResultApplier.class);
		beanContextFactory.registerAutowireableBean(ICUDResultComparer.class, CUDResultComparer.class);

		beanContextFactory.registerAutowireableBean(CompositeIdMixin.class, CompositeIdMixin.class);
		beanContextFactory.registerAutowireableBean(ObjRefMixin.class, ObjRefMixin.class);

		beanContextFactory.registerBean(SecurityScopeProvider.class).autowireable(ISecurityScopeProvider.class, ISecurityScopeChangeListenerExtendable.class);
		beanContextFactory.registerBean(SecurityActivation.class).autowireable(ISecurityActivation.class);

		if (changeControllerClass == null)
		{
			changeControllerClass = ChangeController.class;
		}
		IBeanConfiguration changeController = beanContextFactory.registerBean(changeControllerClass).autowireable(IChangeController.class,
				IChangeControllerExtendable.class);
		beanContextFactory.link(changeController).to(IMergeListenerExtendable.class);

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
		beanContextFactory.registerBean(CacheModification.class).autowireable(ICacheModification.class);

		beanContextFactory.registerAutowireableBean(IObjRefHelper.class, ObjRefHelper.class);
		beanContextFactory.registerBean(CUDResultHelper.class).autowireable(ICUDResultHelper.class, ICUDResultExtendable.class);

		beanContextFactory.registerBean(EntityMetaDataReader.class).autowireable(IEntityMetaDataReader.class);

		beanContextFactory.registerBean(MergeServiceRegistry.class).autowireable(IMergeService.class, IMergeServiceExtensionExtendable.class,
				IMergeListenerExtendable.class, IMergeTimeProvider.class);

		IBeanConfiguration valueObjectMap = beanContextFactory.registerBean(ValueObjectMap.class);

		beanContextFactory
				.registerBean(EntityMetaDataProvider.class)
				.propertyRef("ValueObjectMap", valueObjectMap)
				.autowireable(IEntityMetaDataProvider.class, IEntityMetaDataRefresher.class, IValueObjectConfigExtendable.class,
						IEntityLifecycleExtendable.class, ITechnicalEntityTypeExtendable.class, IEntityMetaDataExtendable.class, EntityMetaDataProvider.class,
						IEntityInstantiationExtensionExtendable.class);

		beanContextFactory.registerBean(INDEPENDENT_META_DATA_READER, IndependentEntityMetaDataReader.class).precedence(PrecedenceType.HIGH);

		if (!independentMetaData)
		{
			IBeanConfiguration entityMetaDataConverter = beanContextFactory.registerBean(EntityMetaDataConverter.class);
			DedicatedConverterUtil.biLink(beanContextFactory, entityMetaDataConverter, EntityMetaData.class, EntityMetaDataTransfer.class);

			beanContextFactory.registerBean(REMOTE_ENTITY_METADATA_PROVIDER, EntityMetaDataClient.class);
		}
		else
		{
		}

		IBeanConfiguration ormConfigGroupProvider = beanContextFactory.registerBean(OrmConfigGroupProvider.class).autowireable(IOrmConfigGroupProvider.class);
		beanContextFactory.link(ormConfigGroupProvider, OrmConfigGroupProvider.handleClearAllCachesEvent).to(IEventListenerExtendable.class)
				.with(ClearAllCachesEvent.class);

		IBeanConfiguration ormXmlReaderLegathy = beanContextFactory.registerBean(OrmXmlReaderLegathy.class);
		ExtendableBean.registerExtendableBean(beanContextFactory, IOrmXmlReaderRegistry.class, IOrmXmlReaderExtendable.class)//
				.propertyRef(ExtendableBean.P_DEFAULT_BEAN, ormXmlReaderLegathy);
		IBeanConfiguration ormXmlReader20BC = beanContextFactory.registerBean(OrmXmlReader20.class);
		beanContextFactory.link(ormXmlReader20BC).to(IOrmXmlReaderExtendable.class).with(OrmXmlReader20.ORM_XML_NS);

		beanContextFactory.registerBean(XmlConfigUtil.class).autowireable(IXmlConfigUtil.class);

		beanContextFactory.registerBean(RelationProvider.class).autowireable(IRelationProvider.class, INoEntityTypeExtendable.class);

		beanContextFactory.registerBean(MemberTypeProvider.class).autowireable(IMemberTypeProvider.class, IIntermediateMemberTypeProvider.class);
		beanContextFactory.registerBean(EmbeddedMemberMixin.class).autowireable(EmbeddedMemberMixin.class);

		beanContextFactory.registerBean(ObjRefFactory.class).autowireable(IObjRefFactory.class);
		IBeanConfiguration objRefObjectCopierExtension = beanContextFactory.registerBean(ObjRefObjectCopierExtension.class);
		beanContextFactory.link(objRefObjectCopierExtension).to(IObjectCopierExtendable.class).with(IObjRef.class).optional();

		Class<?> entityFactoryType = this.entityFactoryType;
		if (entityFactoryType == null)
		{
			entityFactoryType = EntityFactory.class;
		}
		beanContextFactory.registerBean("entityFactory", entityFactoryType).autowireable(IEntityFactory.class);

		beanContextFactory.registerBean(ObjRefStoreEntryProvider.class).autowireable(IObjRefStoreEntryProvider.class);

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
}
