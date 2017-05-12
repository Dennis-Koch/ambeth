using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Cache.Rootcachevalue
{
    public abstract class RootCacheValueFactoryDelegate
    {
        public abstract RootCacheValue CreateRootCacheValue(IEntityMetaData metaData);
    }
}
