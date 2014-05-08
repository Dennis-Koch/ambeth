using System;

namespace De.Osthus.Ambeth.Cache
{
    [Flags]
    public enum CacheDirective
    {
        None = 0,
        FailEarly = 1,
        ReturnMisses = 2,
        NoResult = 4,
        LoadContainerResult = 8,
        CacheValueResult = 16,
        FailInCacheHierarchy = 32
    }
}