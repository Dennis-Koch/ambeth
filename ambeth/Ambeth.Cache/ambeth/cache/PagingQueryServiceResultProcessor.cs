using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Filter.Model;

namespace De.Osthus.Ambeth.Cache
{
    public class PagingQueryServiceResultProcessor : IServiceResultProcessor
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public Object ProcessServiceResult(Object result, IList<Object> entities, Type expectedType, Object[] serviceRequestArgs)
        {
            IPagingResponse pagingResponse = (IPagingResponse)result;

            pagingResponse.RefResult = null;
            pagingResponse.Result = entities;
            return pagingResponse;
        }
    }
}