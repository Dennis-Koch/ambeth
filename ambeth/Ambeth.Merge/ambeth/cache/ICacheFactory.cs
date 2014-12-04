using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Transfer;

namespace De.Osthus.Ambeth.Cache
{
    public interface ICacheFactory
    {
        IDisposableCache CreatePrivileged(CacheFactoryDirective cacheFactoryDirective, String name);

        IDisposableCache CreatePrivileged(CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware, bool? useWeakEntries, String name);

        IDisposableCache Create(CacheFactoryDirective cacheFactoryDirective, String name);

        IDisposableCache Create(CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware, bool? useWeakEntries, String name);
    }
}