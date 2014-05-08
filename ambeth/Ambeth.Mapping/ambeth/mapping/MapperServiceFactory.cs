using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Mapping
{
    public class MapperServiceFactory : IMapperServiceFactory
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IServiceContext BeanContext { protected get; set; }

        [Autowired]
        public ICacheFactory CacheFactory { protected get; set; }

        //[Autowired]
        //public ICacheProvider CacheProvider { protected get; set; }
    
        public IMapperService Create()
        {
            ICache cache = CacheFactory.Create(CacheFactoryDirective.NoDCE);
            //ICache cache = CacheProvider.GetCurrentCache();

            IMapperService mapperService = BeanContext.RegisterAnonymousBean<ModelTransferMapper>().PropertyValue("ChildCache", cache).PropertyValue("WritableCache", cache).Finish();
            IMapperService mapperServiceReference = new MapperServiceWeakReference(mapperService);
            return mapperServiceReference;
        }
    }
}
