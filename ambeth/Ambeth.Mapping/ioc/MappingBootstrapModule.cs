using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Mapping;
using De.Osthus.Ambeth.Merge.Config;

namespace De.Osthus.Ambeth.Ioc
{
    [FrameworkModule]
    public class MappingBootstrapModule : IInitializingBootstrapModule
    {
        [Property(ServiceConfigurationConstants.GenericTransferMapping, DefaultValue = "false")]
        public virtual bool GenericTransferMapping { get; set; }

        public virtual void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            if (GenericTransferMapping)
            {
                beanContextFactory.RegisterBean<ValueObjectConfigReader>("valueObjectConfigReader");
                beanContextFactory.Link("valueObjectConfigReader").To<IEventListenerExtendable>().With(typeof(EntityMetaDataAddedEvent));

                beanContextFactory.RegisterBean<ListTypeHelper>("listTypeHelper").Autowireable<IListTypeHelper>();
                beanContextFactory.RegisterBean<MapperServiceFactory>("mapperServiceFactory").Autowireable<IMapperServiceFactory>();

                beanContextFactory.RegisterBean<ExtendableBean>("mapperExtensionRegistry")
                        .Autowireable<IDedicatedMapperExtendable>().Autowireable<IDedicatedMapperRegistry>()
                        .PropertyValue(ExtendableBean.P_EXTENDABLE_TYPE, typeof(IDedicatedMapperExtendable))
                        .PropertyValue(ExtendableBean.P_PROVIDER_TYPE, typeof(IDedicatedMapperRegistry));
            }
        }
    }
}