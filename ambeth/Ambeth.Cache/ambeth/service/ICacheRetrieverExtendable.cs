using System;

namespace De.Osthus.Ambeth.Service
{
    public interface ICacheRetrieverExtendable
    {
        void RegisterCacheRetriever(ICacheRetriever cacheRetriever, Type handledType);

        void UnregisterCacheRetriever(ICacheRetriever cacheRetriever, Type handledType);
    }
}