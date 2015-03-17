using System;

namespace De.Osthus.Ambeth.Objrefstore
{
    public abstract class IObjRefStoreEntryProvider
    {
        public abstract ObjRefStore CreateObjRefStore(Type entityType, sbyte idIndex, Object id);

        public abstract ObjRefStore CreateObjRefStore(Type entityType, sbyte idIndex, Object id, ObjRefStore nextEntry);
    }
}