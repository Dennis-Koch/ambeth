using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;

namespace De.Osthus.Ambeth.Mapping
{
    public class MapperServiceFactory : IMapperServiceFactory
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IServiceContext BeanContext { protected get; set; }

        public IMapperService Create()
        {
            IMapperService mapperService = BeanContext.RegisterBean<ModelTransferMapper>().Finish();
            IMapperService mapperServiceReference = new MapperServiceWeakReference(mapperService);
            return mapperServiceReference;
        }
    }
}
