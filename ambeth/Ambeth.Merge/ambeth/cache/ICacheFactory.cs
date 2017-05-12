using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Threading;

namespace De.Osthus.Ambeth.Cache
{
    public interface ICacheFactory
    {
		IDisposableCache WithParent(ICache parent, IResultingBackgroundWorkerDelegate<IDisposableCache> runnable);

        IDisposableCache CreatePrivileged(CacheFactoryDirective cacheFactoryDirective, String name);

        IDisposableCache CreatePrivileged(CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware, bool? useWeakEntries, String name);

        IDisposableCache Create(CacheFactoryDirective cacheFactoryDirective, String name);

        IDisposableCache Create(CacheFactoryDirective cacheFactoryDirective, bool foreignThreadAware, bool? useWeakEntries, String name);
    }
}