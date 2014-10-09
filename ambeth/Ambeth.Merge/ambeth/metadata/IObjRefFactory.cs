using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Merge.Model;
using System;

namespace De.Osthus.Ambeth.Metadata
{
    public abstract class IObjRefFactory
    {
        public abstract IPreparedObjRefFactory PrepareObjRefFactory(Type entityType, int idIndex);

        public abstract IObjRef CreateObjRef(Type entityType, int idIndex, Object id, Object version);

        public abstract IObjRef CreateObjRef(AbstractCacheValue cacheValue);

        public abstract IObjRef Dup(IObjRef objRef);
    }
}