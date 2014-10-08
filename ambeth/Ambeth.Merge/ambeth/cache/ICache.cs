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
        /// <summary>
        /// Checks whether this cache instance provides security-filtered or unfiltered content
        /// </summary>
        bool Privileged { get; }

        /// <summary>
        /// If the given cache instance is some kind of proxy or thread-local implementation this method retrieves the internally bound real cache instance. This
	    /// method is intended to be used for performance critical algorithms where multiple calls through the proxy implementation can be skipped.<br/>
	    /// <br/>
	    /// CAUTION: The resulting instance is not intended to be used solely within the method which did this call. To be precise: Do NOT pass the resulting
	    /// instance to any other method or bean as an argument and do NOT store the resulting instance on an object field. Leave it solely as a method stack
	    /// variable
        /// </summary>
        ICache CurrentCache { get; }

        void CascadeLoadPath(Type entityType, String relationPath);

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