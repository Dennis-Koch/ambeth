using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Cache.Rootcachevalue
{
    public interface IRootCacheValueFactory
    {
        RootCacheValue CreateRootCacheValue(IEntityMetaData metaData);
    }
}
