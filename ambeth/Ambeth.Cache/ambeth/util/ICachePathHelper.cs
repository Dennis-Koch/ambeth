using De.Osthus.Ambeth.Collections;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Util
{
    public interface ICachePathHelper
    {
        void BuildCachePath(Type entityType, String memberToInitialize, CHashSet<AppendableCachePath> cachePaths);

	    IPrefetchState EnsureInitializedRelations(Object objects, ILinkedMap<Type, PrefetchPath[]> entityTypeToPrefetchSteps);

	    AppendableCachePath CopyCachePathToAppendable(PrefetchPath cachePath);

        PrefetchPath[] CopyAppendableToCachePath(CHashSet<AppendableCachePath> children);

	    PrefetchPath CopyAppendableToCachePath(AppendableCachePath cachePath);

	    void UnionCachePath(AppendableCachePath cachePath, AppendableCachePath other);
    }
}