using De.Osthus.Ambeth.Privilege.Model;
using System;

namespace De.Osthus.Ambeth.Cache
{
    public interface IRootCache : ICache, ICacheIntern, IWritableCache
    {
        bool ApplyValues(Object targetObject, ICacheIntern targetCache, IPrivilege privilege);

        IRootCache CurrentRootCache { get; }

        IRootCache Parent { get; }
    }
}