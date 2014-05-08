using System;
using System.Net;

namespace De.Osthus.Ambeth.Merge
{
    public interface IUnmodifiedObjectProvider
    {
        Object GetUnmodifiedObject(Type type, Object id);

        Object GetUnmodifiedObject(Object modifiedObject);
    }
}
