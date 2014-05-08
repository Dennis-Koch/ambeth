using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Cache.Model;

namespace De.Osthus.Ambeth.Cache
{
    public interface IServiceResultProcessor
    {
        Object ProcessServiceResult(Object result, IList<Object> entities, Type expectedType, Object[] serviceRequestArgs);
    }
}
