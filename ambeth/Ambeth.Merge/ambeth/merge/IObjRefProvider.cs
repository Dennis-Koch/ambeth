using De.Osthus.Ambeth.Merge.Model;
using System;

namespace De.Osthus.Ambeth.Merge
{
    public interface IObjRefProvider
    {
        IObjRef GetORI(Object obj, IEntityMetaData metaData);
    }
}
