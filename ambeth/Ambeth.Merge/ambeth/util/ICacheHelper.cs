using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Util
{
    public interface ICacheHelper
    {
        [Obsolete]
        IPrefetchConfig CreatePrefetch();

        [Obsolete]
        IPrefetchState Prefetch(Object objects, IDictionary<Type, IList<String>> typeToPathToInitialize);
        
        Object CreateInstanceOfTargetExpectedType(Type expectedType, Type elementType);

        Object ConvertResultListToExpectedType(IList<Object> resultList, Type expectedType, Type elementType);

        Object[] ExtractPrimitives(IEntityMetaData metaData, Object obj);

        IObjRef[][] ExtractRelations(IEntityMetaData metaData, Object obj);

        IObjRef[][] ExtractRelations(IEntityMetaData metaData, Object obj, IList<Object> relationValues);
    }
}