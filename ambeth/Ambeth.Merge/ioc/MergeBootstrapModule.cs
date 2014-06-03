using System;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Converter.Merge;
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

namespace De.Osthus.Ambeth.Ioc
{
    [FrameworkModule]
    public class MergeBootstrapModule : IInitializingBootstrapModule
    {
        public static readonly String MERGE_CACHE_FACTORY = "cacheFactory.merge";

        public static readonly String INDEPENDENT_META_DATA_READER = "independentEntityMetaDataReader";

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
            beanContextFactory.RegisterAutowireableBean<IMergeProcess, MergeProcess>().PropertyRefs(MERGE_CACHE_FACTORY);
            
            beanContextFactory.RegisterAutowireableBean<CompositeIdTemplate, CompositeIdTemplate>();
            
            beanContextFactory.RegisterBean<XmlConfigUtil>("xmlConfigUtil").Autowireable<IXmlConfigUtil>();
            
            beanContextFactory.RegisterBean<CacheModification>("cacheModification").Autowireable<ICacheModification>();

            beanContextFactory.RegisterBean<RelationProvider>("relationProvider").Autowireable<IRelationProvider>();
            beanContextFactory.RegisterAutowireableBean<IObjRefHelper, ORIHelper>();
            beanContextFactory.RegisterBean<CUDResultHelper>("cudResultHelper").Autowireable(typeof(ICUDResultHelper), typeof(ICUDResultExtendable));

        	beanContextFactory.RegisterBean<EntityMetaDataReader>("entityMetaDataReader").Autowireable<IEntityMetaDataReader>();

            IBeanConfiguration valueObjectMap = beanContextFactory.RegisterAnonymousBean<ValueObjectMap>();

            if (!IndependentMetaData)
            {
                beanContextFactory.RegisterBean<EntityMetaDataConverter>("entityMetaDataConverter");
                DedicatedConverterUtil.BiLink(beanContextFactory, "entityMetaDataConverter", typeof(EntityMetaData), typeof(EntityMetaDataTransfer));

                beanContextFactory.RegisterBean<EntityMetaDataClient>("entityMetaDataClient");

                IBeanConfiguration beanConfig = beanContextFactory.RegisterBean<EntityMetaDataCache>("entityMetaDataCache")
                    .PropertyRefs("entityMetaDataClient").PropertyRef("ValueObjectMap", valueObjectMap)
                    .Autowireable<IEntityMetaDataProvider>();
                if (GenericTransferMapping)
                {
                    beanConfig.Autowireable<IValueObjectConfigExtendable>();
                }
            }
            else
            {
			    beanContextFactory.RegisterBean<IndependentEntityMetaDataReader>(INDEPENDENT_META_DATA_READER);

                beanContextFactory.RegisterBean<EntityMetaDataProvider>("independentMetaDataProvider")
                    .PropertyRef("ValueObjectMap", valueObjectMap)
                    .Autowireable<IEntityMetaDataProvider>()
                    .Autowireable<IEntityMetaDataExtendable>()
                    .Autowireable<IValueObjectConfigExtendable>();

                beanContextFactory.RegisterBean("ormXmlReader", typeof(ExtendableBean)).PropertyValue(ExtendableBean.P_PROVIDER_TYPE, typeof(IOrmXmlReaderRegistry))
                        .PropertyValue(ExtendableBean.P_EXTENDABLE_TYPE, typeof(IOrmXmlReaderExtendable))
                        .PropertyRef(ExtendableBean.P_DEFAULT_BEAN, "ormXmlReaderLegathy").Autowireable(typeof(IOrmXmlReaderRegistry), typeof(IOrmXmlReaderExtendable));
                beanContextFactory.RegisterBean<OrmXmlReaderLegathy>("ormXmlReaderLegathy");
                beanContextFactory.RegisterBean<OrmXmlReader20>("ormXmlReader_2.0");
                beanContextFactory.Link("ormXmlReader_2.0").To<IOrmXmlReaderExtendable>().With(OrmXmlReader20.ORM_XML_NS);
            }
            
            Type entityFactoryType = this.EntityFactoryType;
		    if (entityFactoryType == null)
		    {
			    entityFactoryType = typeof(EntityFactory);
		    }
		    IBeanConfiguration entityFactoryBC = beanContextFactory.RegisterBean("entityFactory", entityFactoryType).Autowireable<IEntityFactory>();
		    if (typeof(IEntityFactoryExtensionExtendable).IsAssignableFrom(entityFactoryType))
		    {
			    entityFactoryBC.Autowireable<IEntityFactoryExtensionExtendable>();
		    }

            if (IsNetworkClientMode && IsMergeServiceBeanActive)
            {
                beanContextFactory.RegisterBean<ClientServiceBean>("mergeServiceWCF")
                    .PropertyValue("Interface", typeof(IMergeService))
                    .PropertyValue("SyncRemoteInterface", typeof(IMergeServiceWCF))
                    .PropertyValue("AsyncRemoteInterface", typeof(IMergeClient))
                    .Autowireable<IMergeService>();
                // beanContextFactory.registerBean<MergeServiceDelegate>("mergeService").autowireable<IMergeService>();
            }
            else if (IsNetworkClientMode)
            {
                beanContextFactory.RegisterBean<MergeServiceRegistry>("mergeService").IgnoreProperties("DefaultMergeService").Autowireable(typeof(IMergeService), typeof(IMergeServiceExtendable));
            }
        }
    }
}