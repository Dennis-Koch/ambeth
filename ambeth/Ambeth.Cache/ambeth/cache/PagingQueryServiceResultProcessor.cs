using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Filter.Model;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Exceptions;

namespace De.Osthus.Ambeth.Cache
{
    public class PagingQueryServiceResultProcessor : IServiceResultProcessor
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public Object ProcessServiceResult(Object result, IList<IObjRef> objRefs, IList<Object> entities, Type expectedType, Object[] serviceRequestArgs,
			Attribute annotation)
        {
            IPagingResponse pagingResponse = (IPagingResponse)result;

			QueryResultType queryResultType = QueryResultType.REFERENCES;
			if (annotation is FindAttribute)
			{
				queryResultType = ((FindAttribute)annotation).ResultType;
			}
			switch (queryResultType)
			{
				case QueryResultType.BOTH:
					pagingResponse.RefResult = objRefs;
					pagingResponse.Result = entities;
					break;
				case QueryResultType.ENTITIES:
					pagingResponse.RefResult = null;
					pagingResponse.Result = entities;
					break;
				case QueryResultType.REFERENCES:
					pagingResponse.RefResult = objRefs;
					pagingResponse.Result = null;
					break;
				default:
					throw RuntimeExceptionUtil.CreateEnumNotSupportedException(queryResultType);
			}
			return pagingResponse;
        }
    }
}