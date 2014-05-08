using System.Collections.Generic;
namespace De.Osthus.Ambeth.Cache
{
    public interface IFirstLevelCacheManager
    {
        IList<IWritableCache> SelectFirstLevelCaches();
    }
}