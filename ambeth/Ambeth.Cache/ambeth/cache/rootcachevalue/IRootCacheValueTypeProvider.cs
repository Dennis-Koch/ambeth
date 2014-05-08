using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Cache.Rootcachevalue
{
    public interface IRootCacheValueTypeProvider
    {
	    ConstructorInfo GetRootCacheValueType(Type entityType);
    }
}
