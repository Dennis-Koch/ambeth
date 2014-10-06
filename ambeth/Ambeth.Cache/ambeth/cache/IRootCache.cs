using System;

namespace De.Osthus.Ambeth.Cache
{
    public interface IRootCache : ICache, ICacheIntern, IWritableCache
    {
        bool ApplyValues(Object targetObject, ICacheIntern targetCache);

        IRootCache CurrentRootCache { get; }
    }
}