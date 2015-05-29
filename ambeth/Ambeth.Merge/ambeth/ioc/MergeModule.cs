using System;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Converter.Merge;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Config;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Orm;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Remote;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Mixin;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Util.Xml;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Objrefstore;
using De.Osthus.Ambeth.Copy;
using De.Osthus.Ambeth.Security;

namespace De.Osthus.Ambeth.Ioc
{
    [FrameworkModule]
    public class MergeModule : IInitializingModule
    {
        public const String INDEPENDENT_META_DATA_READER = "independentEntityMetaDataReader";

        public const String REMOTE_ENTITY_METADATA_PROVIDER = "entityMetaDataProvider.remote";

        public const String DEFAULT_MERGE_SERVICE_EXTENSION = "mergeServiceExtension.default";

        [Property(ServiceConfigurationConstants.NetworkClientMode, DefaultValue = "false")]
        public virtual bool IsNetworkClientMode { get; set; }

        [Property(MergeConfigurationConstants.MergeServiceBeanActive, DefaultValue = "true")]
        public virtual bool IsMergeServiceBeanActive { get; set; }

        [Property(ServiceConfigurationConstants.GenericTransferMapping, DefaultValue = "false")]
        public virtual bool GenericTransferMapping { get; set; }

        [Property(ServiceConfigurationConstants.IndependentMetaData, DefaultValue = "false")]
        public virtual bool IndependentMetaData { get; set; }

        [Property(MergeConfigurationConstants.EntityFactoryType, Mandatory = false)]
        public Type EntityFactoryType { get; set; }

        public virtual void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            beanContextFactory.RegisterAutowireableBean<IMergeController, MergeController>();
            beanContextFactory.RegisterAutowireableBean<IMergeProcess, MergeProcess>();
            beanContextFactory.RegisterAutowireableBean<ICUDResultApplier, CUDResultApplier>();
		    beanContextFactory.RegisterAutowireableBean<ICUDResultComparer, CUDResultComparer>();

            beanContextFactory.RegisterAutowireableBean<CompositeIdMixin, CompositeIdMixin>();
            beanContextFactory.RegisterAutowireableBean<ObjRefMixin, ObjRefMixin>();

            beanContextFactory.RegisterBean<SecurityScopeProvider>().Autowireable(typeof(ISecurityScopeProvider), typeof(ISecurityScopeChangeListenerExtendable));
            beanContextFactory.RegisterBean<SecurityActivation>().Autowireable<ISecurityActivation>();

            beanContextFactory.RegisterBean<CacheModification>().Autowireable<ICacheModification>();

            beanContextFactory.RegisterAutowireableBean<IObjRefHelper, ObjRefHelper>();
            beanContextFactory.RegisterBean<CUDResultHelper>().Autowireable(typeof(ICUDResultHelper), typeof(ICUDResultExtendable));

            beanContextFactory.RegisterBean<EntityMetaDataReader>().Autowireable<IEntityMetaDataReader>();

            beanContextFactory.RegisterBean<MergeServiceRegistry>().Autowireable(typeof(IMergeService), typeof(IMergeServiceExtensionExtendable), typeof(IMergeListenerExtendable),
                typeof(IMergeTimeProvider));

            IBeanConfiguration valueObjectMap = beanContextFactory.RegisterBean<ValueObjectMap>();

            IBeanConfiguration entityMetaDataProvider = beanContextFactory.RegisterBean<EntityMetaDataProvider>()
                .PropertyRef("ValueObjectMap", valueObjectMap)
                .Autowireable<IEntityMetaDataProvider>()
                .Autowireable<IEntityMetaDataRefresher>()
                .Autowireable<IValueObjectConfigExtendable>()
                .Autowireable<IEntityLifecycleExtendable>()
                .Autowireable<ITechnicalEntityTypeExtendable>()
                .Autowireable<IEntityMetaDataExtendable>()
                .Autowireable<IEntityInstantiationExtensionExtendable>();

            beanContextFactory.RegisterBean<IndependentEntityMetaDataReader>(INDEPENDENT_META_DATA_READER).Precedence(PrecedenceType.HIGH);

            if (!IndependentMetaData)
            {
                IBeanConfiguration entityMetaDataConverter = beanContextFactory.RegisterBean<EntityMetaDataConverter>();
                DedicatedConverterUtil.BiLink(beanContextFactory, entityMetaDataConverter, typeof(EntityMetaData), typeof(EntityMetaDataTransfer));

                beanContextFactory.RegisterBean<EntityMetaDataClient>(REMOTE_ENTITY_METADATA_PROVIDER);
            }
            else
            {
            }

			IBeanConfiguration ormConfigGroupProvider = beanContextFactory.RegisterBean<OrmConfigGroupProvider>().Autowireable<IOrmConfigGroupProvider>();
			beanContextFactory.Link(ormConfigGroupProvider, OrmConfigGroupProvider.handleClearAllCachesEvent).To<IEventListenerExtendable>()
					.With(typeof(ClearAllCachesEvent));

       		IBeanConfiguration ormXmlReaderLegathy = beanContextFactory.RegisterBean<OrmXmlReaderLegathy>();
		    ExtendableBean.RegisterExtendableBean(beanContextFactory, typeof(IOrmXmlReaderRegistry), typeof(IOrmXmlReaderExtendable))//
				    .PropertyRef(ExtendableBean.P_DEFAULT_BEAN, ormXmlReaderLegathy);
		    IBeanConfiguration ormXmlReader20BC = beanContextFactory.RegisterBean<OrmXmlReader20>();
		    beanContextFactory.Link(ormXmlReader20BC).To<IOrmXmlReaderExtendable>().With(OrmXmlReader20.ORM_XML_NS);

		    beanContextFactory.RegisterBean<XmlConfigUtil>().Autowireable<IXmlConfigUtil>();

            beanContextFactory.RegisterBean<RelationProvider>().Autowireable(typeof(IRelationProvider), typeof(INoEntityTypeExtendable));

            beanContextFactory.RegisterBean<MemberTypeProvider>().Autowireable<IMemberTypeProvider>().Autowireable<IIntermediateMemberTypeProvider>();
		    beanContextFactory.RegisterBean<EmbeddedMemberMixin>().Autowireable<EmbeddedMemberMixin>();
            
            beanContextFactory.RegisterBean<ObjRefFactory>().Autowireable<IObjRefFactory>();
            IBeanConfiguration objRefObjectCopierExtension = beanContextFactory.RegisterBean<ObjRefObjectCopierExtension>();
		    beanContextFactory.Link(objRefObjectCopierExtension).To<IObjectCopierExtendable>().With(typeof(IObjRef));

            Type entityFactoryType = this.EntityFactoryType;
            if (entityFactoryType == null)
            {
                entityFactoryType = typeof(EntityFactory);
            }
            beanContextFactory.RegisterBean("entityFactory", entityFactoryType).Autowireable<IEntityFactory>();

            beanContextFactory.RegisterBean<ObjRefStoreEntryProvider>().Autowireable<IObjRefStoreEntryProvider>();

            if (IsNetworkClientMode && IsMergeServiceBeanActive)
            {
                IBeanConfiguration remoteMergeServiceExtension = beanContextFactory.RegisterBean<ClientServiceBean>(DEFAULT_MERGE_SERVICE_EXTENSION)
                    .PropertyValue("Interface", typeof(IMergeServiceExtension))
                    .PropertyValue("SyncRemoteInterface", typeof(IMergeServiceWCF))
                    .PropertyValue("AsyncRemoteInterface", typeof(IMergeClient));

                // register to all entities in a "most-weak" manner
                beanContextFactory.Link(remoteMergeServiceExtension).To<IMergeServiceExtensionExtendable>().With(typeof(Object));
            }
        }
    }
}