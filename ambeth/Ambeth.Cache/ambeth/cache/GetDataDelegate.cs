using De.Osthus.Ambeth.Service;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Cache
{
    public delegate IList<R> GetDataDelegate<V, R>(ICacheRetriever cacheRetriever, IList<V> arguments);
}