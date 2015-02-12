using System;
using System.Collections;
using De.Osthus.Ambeth.Merge.Model;
using System.Collections.Generic;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Minerva.Mock
{
    public interface IPersistenceMock
    {
        IList<Object> GetAllIds<T>();

        Lock GetWriteLock();

        void AddObject(IObjRef objRef, IPrimitiveUpdateItem[] primitiveUpdates, IRelationUpdateItem[] relationUpdates, String changedBy, long changedOn);

        void ChangeObject(IObjRef objRef, IPrimitiveUpdateItem[] primitiveUpdates, IRelationUpdateItem[] relationUpdates, String changedBy, long changedOn);

        void RemoveObject(IObjRef objRef);
    }
}
