using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Cache.Model;

namespace De.Osthus.Ambeth.Cache
{
    public interface ICacheIntern : ICache
    {
        Object CreateCacheValueInstance(IEntityMetaData metaData, Object obj);

        void AddDirect(IEntityMetaData metaData, Object id, Object version, Object primitiveFilledObject, Object parentCacheValueOrArray, IObjRef[][] relations);

        Object GetObject(IObjRef oriToGet, ICacheIntern targetCache, CacheDirective cacheDirective);

        IList<Object> GetObjects(IList<IObjRef> orisToGet, ICacheIntern targetCache, CacheDirective cacheDirective);

        IList<IObjRelationResult> GetObjRelations(IList<IObjRelation> objRels, ICacheIntern targetCache, CacheDirective cacheDirective);
        
        bool AcquireHardRefTLIfNotAlready();
        
        void ClearHardRefs(bool acquirementSuccessful);
    }
}