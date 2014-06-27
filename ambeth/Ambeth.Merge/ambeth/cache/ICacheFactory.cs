using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Transfer;

namespace De.Osthus.Ambeth.Cache
{
    public interface ICacheFactory
    {
        IDisposableCache CreatePrivileged(CacheFactoryDirective cacheFactoryDirective);

        IDisposableCache CreatePrivileged(CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware, bool? useWeakEntries);

        IDisposableCache Create(CacheFactoryDirective cacheFactoryDirective);

        IDisposableCache Create(CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware, bool? useWeakEntries);
    }
}