using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Cache.Mock
{
    /**
     * Support for unit tests that do not include jAmbeth.Cache
     */
    public class PrefetchHelperMock : IPrefetchHelper
    {
        public IPrefetchConfig CreatePrefetch()
        {
            return null;
        }

        public IPrefetchState Prefetch(Object objects)
        {
            return null;
        }

        public IPrefetchState Prefetch(Object objects, IDictionary<Type, IList<String>> typeToPathToInitialize)
        {
            return null;
        }

        public ICollection<T> ExtractTargetEntities<T, S>(IEnumerable<S> sourceEntities, String sourceToTargetEntityPropertyPath)
        {
            return null;
        }
    }
}