using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Xml.Test;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.CompositeId;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Merge.Config;
using De.Osthus.Ambeth.Util.Xml;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Orm;
using De.Osthus.Ambeth.Xml;

namespace De.Osthus.Ambeth.Ioc
{
    [FrameworkModule]
    public class XmlBootstrapTestModule : IInitializingBootstrapModule
    {
        public virtual void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            beanContextFactory.RegisterBean<ORIHelper>("oriHelper").Autowireable<IObjRefHelper>();

            beanContextFactory.RegisterBean<DefaultProxyHelper>("proxyHelper").Autowireable<IProxyHelper>();

            beanContextFactory.RegisterBean<RelationProvider>("relationProvider").Autowireable<IRelationProvider>();

            beanContextFactory.RegisterBean<TypeInfoProvider>("typeInfoProvider").Autowireable<ITypeInfoProvider>();

            IBeanConfiguration valueObjectMap = beanContextFactory.RegisterAnonymousBean<ValueObjectMap>();

            beanContextFactory.RegisterBean("independentMetaDataProvider", typeof(EntityMetaDataProvider))
                    .PropertyRef("ValueObjectMap", valueObjectMap)
                    .Autowireable(typeof(IEntityMetaDataProvider))
                    .Autowireable<IEntityMetaDataExtendable>()
                    .Autowireable<IValueObjectConfigExtendable>();
            beanContextFactory.RegisterBean<EntityMetaDataReader>("entityMetaDataReader");
            beanContextFactory.RegisterBean<XmlConfigUtil>("xmlConfigUtil").Autowireable<IXmlConfigUtil>();

            beanContextFactory.RegisterBean("ormXmlReader", typeof(ExtendableBean)).PropertyValue(ExtendableBean.P_PROVIDER_TYPE, typeof(IOrmXmlReaderRegistry))
                    .PropertyValue(ExtendableBean.P_EXTENDABLE_TYPE, typeof(IOrmXmlReaderExtendable))
                    .PropertyRef(ExtendableBean.P_DEFAULT_BEAN, "ormXmlReaderLegathy").Autowireable(typeof(IOrmXmlReaderRegistry), typeof(IOrmXmlReaderExtendable));
            beanContextFactory.RegisterBean<OrmXmlReaderLegathy>("ormXmlReaderLegathy");
            beanContextFactory.RegisterBean<OrmXmlReader20>("ormXmlReader_2.0");
            beanContextFactory.Link("ormXmlReader_2.0").To<IOrmXmlReaderExtendable>().With(OrmXmlReader20.ORM_XML_NS);

            // Mocks

            beanContextFactory.RegisterBean<CacheMock>("cacheMock").Autowireable(typeof(ICache));

            beanContextFactory.RegisterBean<CacheFactoryMock>("cacheFactoryMock").Autowireable(typeof(ICacheFactory));

            beanContextFactory.RegisterBean<EntityFactoryMock>("entityFactoryMock").Autowireable<IEntityFactory>();
            
            beanContextFactory.RegisterBean<XmlTypeHelper>("xmlTypeHelper").Autowireable<IXmlTypeHelper>();

            // Dummy

            beanContextFactory.RegisterBean<EmptyDummy>("emptyDummy").Autowireable<ICompositeIdFactory>().Autowireable<IMergeController>().Autowireable<IPrefetchHelper>();
        }
    }
}
