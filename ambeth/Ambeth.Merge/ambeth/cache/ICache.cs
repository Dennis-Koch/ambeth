using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Cache.Model;

namespace De.Osthus.Ambeth.Cache
{
    public interface ICache
    {
        void CascadeLoadPath(Type entityType, String cascadeLoadPath);

        IList<E> GetObjects<E>(params Object[] ids);

        IList<E> GetObjects<E>(IList<Object> ids);

        IList<Object> GetObjects(Type type, params Object[] ids);

        IList<Object> GetObjects(Type type, IList<Object> ids);

        IList<Object> GetObjects(IList<IObjRef> orisToGet, CacheDirective cacheDirective);

        IList<IObjRelationResult> GetObjRelations(IList<IObjRelation> objRels, CacheDirective cacheDirective);

        Object GetObject(IObjRef oriToGet, CacheDirective cacheDirective);

        Object GetObject(Type type, Object id);

        Object GetObject(Type type, Object id, CacheDirective cacheDirective);

        E GetObject<E>(Object id);

        void GetContent(HandleContentDelegate handleContentDelegate);

        Lock ReadLock { get; }

        Lock WriteLock { get; }
    }

    public delegate void HandleContentDelegate(Type entityType, sbyte idIndex, Object id, Object value);
}