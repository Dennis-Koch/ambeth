using De.Osthus.Ambeth.Collections;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Util
{
    public interface ICachePathHelper
    {
        void BuildCachePath(Type entityType, String memberToInitialize, CHashSet<AppendableCachePath> cachePaths);

	    IPrefetchState EnsureInitializedRelations(Object objects, ILinkedMap<Type, CachePath[]> entityTypeToPrefetchSteps);

	    AppendableCachePath CopyCachePathToAppendable(CachePath cachePath);

        CachePath[] CopyAppendableToCachePath(CHashSet<AppendableCachePath> children);

	    CachePath CopyAppendableToCachePath(AppendableCachePath cachePath);

	    void UnionCachePath(AppendableCachePath cachePath, AppendableCachePath other);
    }
}