using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Cache;

namespace De.Osthus.Ambeth.Merge
{
    public interface IProxyHelper
    {
        Type GetRealType(Type type);

        bool ObjectEquals(Object leftObject, Object rightObject);
    }
}
