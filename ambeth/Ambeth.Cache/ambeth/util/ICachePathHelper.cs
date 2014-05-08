using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Util
{
    public interface ICachePathHelper
    {
        void BuildCachePath(Type entityType, String memberToInitialize, IList<CachePath> cachePaths);

        IPrefetchState EnsureInitializedRelations<V>(Object objects, IDictionary<Type, V> typeToMembersToInitialize) where V : IList<CachePath>;
    }
}