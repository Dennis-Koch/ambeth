using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Cache
{
    public interface IServiceResultProcessor
    {
        Object ProcessServiceResult(Object result, IList<IObjRef> objRefs, IList<Object> entities, Type expectedType, Object[] serviceRequestArgs,
			Attribute annotation);
    }
}
