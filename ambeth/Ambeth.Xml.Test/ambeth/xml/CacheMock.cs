using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Test.Model;
using De.Osthus.Ambeth.Cache.Model;

namespace De.Osthus.Ambeth.Xml.Test
{
    public class CacheMock : ICache, IDisposableCache
    {
        public void Dispose()
        {
            // Intended blank
        }

        public void CascadeLoadPath(Type entityType, string cascadeLoadPath)
        {
            throw new NotImplementedException();
        }

        public IList<E> GetObjects<E>(params object[] ids)
        {
            throw new NotImplementedException();
        }

        public IList<E> GetObjects<E>(IList<object> ids)
        {
            throw new NotImplementedException();
        }

        public IList<object> GetObjects(Type type, params object[] ids)
        {
            throw new NotImplementedException();
        }

        public IList<object> GetObjects(Type type, IList<object> ids)
        {
            throw new NotImplementedException();
        }

        public IList<object> GetObjects(IList<IObjRef> orisToGet, CacheDirective cacheDirective)
        {
            IList<object> objs = new List<object>();

            foreach (IObjRef ori in orisToGet)
            {
                object obj;
                if (ori == null)
                {
                    obj = null;
                }
                else if (ori is IDirectObjRef)
                {
                    obj = ((IDirectObjRef)ori).Direct;
                }
                else
                {
                    obj = GetObject(ori.RealType, ori.Id);
                }
                objs.Add(obj);
            }

            return objs;
        }

        public IList<IObjRelationResult> GetObjRelations(IList<IObjRelation> objRels, CacheDirective cacheDirective)
        {
            throw new NotImplementedException();
        }

        public object GetObject(IObjRef oriToGet, CacheDirective cacheDirective)
        {
            throw new NotImplementedException();
        }

        public object GetObject(Type type, object id)
        {
            object obj = Activator.CreateInstance(type);
            if (obj is Material)
            {
                Material material = obj as Material;
                material.Id = (int)id;
                material.Version = 1;
                material.Buid = "Material " + id;
                material.Name = "Material name " + id;
                material.MaterialGroup = GetObject<MaterialGroup>(id);
            }
            else if (obj is MaterialGroup)
            {
                MaterialGroup materialGroup = obj as MaterialGroup;
                materialGroup.Id = "" + id;
                materialGroup.Version = 1;
                materialGroup.Buid = "MaterialGroup " + id;
                materialGroup.Name = "MaterialGroup name " + id;
            }
            return obj;
        }

        public object GetObject(Type type, object id, CacheDirective cacheDirective)
        {
            throw new NotImplementedException();
        }

        public E GetObject<E>(object id)
        {
            return (E)GetObject(typeof(E), id);
        }

        public void GetContent(HandleContentDelegate handleContentDelegate)
        {
            throw new NotImplementedException();
        }

        public De.Osthus.Ambeth.Util.Lock ReadLock
        {
            get { throw new NotImplementedException(); }
        }

        public De.Osthus.Ambeth.Util.Lock WriteLock
        {
            get { throw new NotImplementedException(); }
        }
    }
}
