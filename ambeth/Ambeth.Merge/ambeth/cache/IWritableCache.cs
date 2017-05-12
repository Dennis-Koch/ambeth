using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Cache
{
    public interface IWritableCache : ICache
    {
        int CacheId { get; set; }

        void Clear();

        IList<Object> Put(Object objectToCache);

        void Remove(IList<IObjRef> oris);

        void Remove(IObjRef ori);

        void Remove(Type type, Object id);

        void RemovePriorVersions(IList<IObjRef> oris);

        void RemovePriorVersions(IObjRef oris);
    }

    public delegate void HandleChildCachesDelegate(ICollection<IWritableCache> childCaches);
}