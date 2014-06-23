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
    public class XmlTestModule : IInitializingModule
    {
        public virtual void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            //beanContextFactory.RegisterBean<DefaultProxyHelper>("proxyHelper").Autowireable<IProxyHelper>();

            //beanContextFactory.RegisterBean<TypeInfoProvider>("typeInfoProvider").Autowireable<ITypeInfoProvider>();

            //beanContextFactory.RegisterBean<RelationProvider>("relationProvider").Autowireable<IRelationProvider>();

            //beanContextFactory.RegisterBean<ORIHelper>("oriHelper").Autowireable<IObjRefHelper>();

            //beanContextFactory.RegisterBean<EntityMetaDataProviderDummy>("entityMetaDataProviderDummy").Autowireable<IEntityMetaDataProvider>();

            //beanContextFactory.RegisterBean<CacheDummy>("cache").Autowireable<ICache>();

            //beanContextFactory.RegisterBean<OriHelperDummy>("oriHelper", ).Autowireable<IObjRefHelper>();

            //beanContextFactory.RegisterBean<EntityFactoryDummy>("entityFactory", ).Autowireable<IEntityFactory>();

            //beanContextFactory.RegisterBean<CacheFactoryDummy>("cacheFactory", ).Autowireable<ICacheFactory>();

            //beanContextFactory.RegisterBean<MergeControllerDummy>("mergeController", ).Autowireable<IMergeController>();

            //beanContextFactory.RegisterBean<PrefetchHelperDummy>("prefetchHelper", ).Autowireable<IPrefetchHelper>();
            
            //// Mocks

            //beanContextFactory.RegisterBean<CacheMock>("cacheMock").Autowireable(typeof(ICache));

            //beanContextFactory.RegisterBean<CacheFactoryMock>("cacheFactoryMock").Autowireable(typeof(ICacheFactory));

            //beanContextFactory.RegisterBean<EntityFactoryMock>("entityFactoryMock").Autowireable<IEntityFactory>();
            
            //beanContextFactory.RegisterBean<XmlTypeHelper>("xmlTypeHelper").Autowireable<IXmlTypeHelper>();

            //// Dummy

            //beanContextFactory.RegisterBean<EmptyDummy>("emptyDummy").Autowireable<ICompositeIdFactory>().Autowireable<IMergeController>().Autowireable<IPrefetchHelper>();
        }
    }
}
