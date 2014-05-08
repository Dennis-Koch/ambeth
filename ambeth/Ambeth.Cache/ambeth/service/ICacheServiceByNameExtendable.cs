using System;

namespace De.Osthus.Ambeth.Service
{
    public interface ICacheServiceByNameExtendable
    {
        void RegisterCacheService(ICacheService cacheService, String serviceName);

        void UnregisterCacheService(ICacheService cacheService, String serviceName);
    }
}