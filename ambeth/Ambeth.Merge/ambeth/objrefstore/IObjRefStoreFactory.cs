using System;

namespace De.Osthus.Ambeth.Objrefstore
{
    public abstract class IObjRefStoreFactory
    {
        public abstract ObjRefStore CreateObjRef();

        public abstract ObjRefStore CreateObjRef(Object id, Object version);
    }
}
