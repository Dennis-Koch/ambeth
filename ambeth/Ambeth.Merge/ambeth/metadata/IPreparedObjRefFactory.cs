using De.Osthus.Ambeth.Merge.Model;
using System;

namespace De.Osthus.Ambeth.Metadata
{
    public interface IPreparedObjRefFactory
    {
        IObjRef CreateObjRef(Object id, Object version);

        IObjRef CreateObjRef();
    }
}
