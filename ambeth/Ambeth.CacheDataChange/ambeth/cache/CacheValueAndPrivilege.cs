using De.Osthus.Ambeth.Cache.Rootcachevalue;
using De.Osthus.Ambeth.Privilege.Model;

namespace De.Osthus.Ambeth.Cache
{
    public class CacheValueAndPrivilege
    {
        public readonly RootCacheValue cacheValue;

        public readonly IPrivilege privilege;

        public CacheValueAndPrivilege(RootCacheValue cacheValue, IPrivilege privilege)
        {
            this.cacheValue = cacheValue;
            this.privilege = privilege;
        }
    }
}