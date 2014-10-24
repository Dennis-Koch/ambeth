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
using De.Osthus.Ambeth.Template;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Util.Xml;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Objrefstore;
using De.Osthus.Ambeth.Copy;

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

            beanContextFactory.RegisterAutowireableBean<CompositeIdTemplate, CompositeIdTemplate>();
            beanContextFactory.RegisterAutowireableBean<ObjRefTemplate, ObjRefTemplate>();

            beanContextFactory.RegisterBean<CacheModification>("cacheModification").Autowireable<ICacheModification>();

            beanContextFactory.RegisterAutowireableBean<IObjRefHelper, ObjRefHelper>();
            beanContextFactory.RegisterBean<CUDResultHelper>("cudResultHelper").Autowireable(typeof(ICUDResultHelper), typeof(ICUDResultExtendable));

            beanContextFactory.RegisterBean<EntityMetaDataReader>("entityMetaDataReader").Autowireable<IEntityMetaDataReader>();

            beanContextFactory.RegisterAnonymousBean<MergeServiceRegistry>().Autowireable(typeof(IMergeService), typeof(IMergeServiceExtensionExtendable));

            IBeanConfiguration valueObjectMap = beanContextFactory.RegisterAnonymousBean<ValueObjectMap>();

            IBeanConfiguration entityMetaDataProvider = beanContextFactory.RegisterAnonymousBean<EntityMetaDataProvider>()
                .PropertyRef("ValueObjectMap", valueObjectMap)
                .Autowireable<IEntityMetaDataProvider>()
                .Autowireable<IEntityMetaDataRefresher>()
                .Autowireable<IValueObjectConfigExtendable>()
                .Autowireable<IEntityLifecycleExtendable>()
                .Autowireable<ITechnicalEntityTypeExtendable>()
                .Autowireable<IEntityMetaDataExtendable>()
                .Autowireable<IEntityInstantiationExtensionExtendable>();

            if (!IndependentMetaData)
            {
                beanContextFactory.RegisterBean<EntityMetaDataConverter>("entityMetaDataConverter");
                DedicatedConverterUtil.BiLink(beanContextFactory, "entityMetaDataConverter", typeof(EntityMetaData), typeof(EntityMetaDataTransfer));

                beanContextFactory.RegisterBean<EntityMetaDataClient>(REMOTE_ENTITY_METADATA_PROVIDER);
            }
            else
            {
                beanContextFactory.RegisterBean<IndependentEntityMetaDataReader>(INDEPENDENT_META_DATA_READER).Precedence(PrecedenceType.HIGH);

                beanContextFactory.RegisterBean("ormXmlReader", typeof(ExtendableBean)).PropertyValue(ExtendableBean.P_PROVIDER_TYPE, typeof(IOrmXmlReaderRegistry))
                        .PropertyValue(ExtendableBean.P_EXTENDABLE_TYPE, typeof(IOrmXmlReaderExtendable))
                        .PropertyRef(ExtendableBean.P_DEFAULT_BEAN, "ormXmlReaderLegathy").Autowireable(typeof(IOrmXmlReaderRegistry), typeof(IOrmXmlReaderExtendable));
                beanContextFactory.RegisterBean<OrmXmlReaderLegathy>("ormXmlReaderLegathy");
			    IBeanConfiguration ormXmlReader20BC = beanContextFactory.RegisterAnonymousBean<OrmXmlReader20>();
			    beanContextFactory.Link(ormXmlReader20BC).To<IOrmXmlReaderExtendable>().With(OrmXmlReader20.ORM_XML_NS);

			    beanContextFactory.RegisterBean<XmlConfigUtil>("xmlConfigUtil").Autowireable<IXmlConfigUtil>();
            }
            beanContextFactory.RegisterAnonymousBean<RelationProvider>().Autowireable<IRelationProvider>();

            beanContextFactory.RegisterAnonymousBean<MemberTypeProvider>().Autowireable<IMemberTypeProvider>().Autowireable<IIntermediateMemberTypeProvider>();
		    beanContextFactory.RegisterAnonymousBean<EmbeddedMemberTemplate>().Autowireable<EmbeddedMemberTemplate>();
            
            beanContextFactory.RegisterAnonymousBean<ObjRefFactory>().Autowireable<IObjRefFactory>();
            IBeanConfiguration objRefObjectCopierExtension = beanContextFactory.RegisterAnonymousBean<ObjRefObjectCopierExtension>();
		    beanContextFactory.Link(objRefObjectCopierExtension).To<IObjectCopierExtendable>().With(typeof(IObjRef));

            Type entityFactoryType = this.EntityFactoryType;
            if (entityFactoryType == null)
            {
                entityFactoryType = typeof(EntityFactory);
            }
            beanContextFactory.RegisterBean("entityFactory", entityFactoryType).Autowireable<IEntityFactory>();

            beanContextFactory.RegisterAnonymousBean<ObjRefStoreEntryProvider>().Autowireable<IObjRefStoreEntryProvider>();

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