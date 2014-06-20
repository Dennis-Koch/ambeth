using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Cache.Mock
{
    /**
     * Support for unit tests that do not include jAmbeth.Cache
     */
    public class CacheMock : ICache
    {
        public void CascadeLoadPath(Type entityType, String cascadeLoadPath)
        {
            throw new NotImplementedException();
        }

        public IList<E> GetObjects<E>(params Object[] ids)
        {
            throw new NotImplementedException();
        }

        public IList<E> GetObjects<E>(IList<Object> ids)
        {
            throw new NotImplementedException();
        }

        public IList<Object> GetObjects(Type type, params Object[] ids)
        {
            throw new NotImplementedException();
        }

        public IList<Object> GetObjects(Type type, IList<Object> ids)
        {
            throw new NotImplementedException();
        }

        public IList<Object> GetObjects(IList<IObjRef> orisToGet, CacheDirective cacheDirective)
        {
            throw new NotImplementedException();
        }

        public IList<IObjRelationResult> GetObjRelations(IList<IObjRelation> objRels, CacheDirective cacheDirective)
        {
            throw new NotImplementedException();
        }

        public Object GetObject(IObjRef oriToGet, CacheDirective cacheDirective)
        {
            throw new NotImplementedException();
        }

        public Object GetObject(Type type, Object id)
        {
            throw new NotImplementedException();
        }

        public Object GetObject(Type type, Object id, CacheDirective cacheDirective)
        {
            throw new NotImplementedException();
        }

        public E GetObject<E>(Object id)
        {
            throw new NotImplementedException();
        }

        public void GetContent(HandleContentDelegate handleContentDelegate)
        {
            throw new NotImplementedException();
        }

        public Lock ReadLock
        {
            get { throw new NotImplementedException(); }
        }

        public Lock WriteLock
        {
            get { throw new NotImplementedException(); }
        }
    }
}