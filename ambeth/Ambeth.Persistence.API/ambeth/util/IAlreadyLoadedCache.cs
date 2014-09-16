using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Merge.Model;
using System;

namespace De.Osthus.Ambeth.Util
{
    public interface IAlreadyLoadedCache
    {
        void Clear();

        int Size();

        IAlreadyLoadedCache Snapshot();

        void CopyTo(IAlreadyLoadedCache targetAlCache);

        ILoadContainer GetObject(sbyte idNameIndex, Object id, Type type);

        ILoadContainer GetObject(IdTypeTuple idTypeTuple);

        IObjRef GetRef(sbyte idNameIndex, Object id, Type type);

        IObjRef GetRef(IdTypeTuple idTypeTuple);

        void Add(sbyte idNameIndex, Object id, Type type, IObjRef objRef);

        void Add(IdTypeTuple idTypeTuple, IObjRef objRef);

        void Add(sbyte idNameIndex, Object persistentId, Type type, IObjRef objRef, ILoadContainer loadContainer);

        void Add(IdTypeTuple idTypeTuple, IObjRef objRef, ILoadContainer loadContainer);

        void Replace(sbyte idNameIndex, Object persistentId, Type type, ILoadContainer loadContainer);

        void Replace(IdTypeTuple idTypeTuple, ILoadContainer loadContainer);
    }
}
