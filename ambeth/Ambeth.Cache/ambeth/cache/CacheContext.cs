using System;

namespace De.Osthus.Ambeth.Cache
{
    [AttributeUsage(AttributeTargets.Class | AttributeTargets.Interface)]
    public class CacheContext : Attribute
    {
        public CacheContext(CacheType cacheType)
        {
            CacheType = cacheType;
        }

        public CacheContext()
        {
            CacheType = CacheType.PROTOTYPE;
        }

        public CacheType CacheType { get; private set; }
    }
}
