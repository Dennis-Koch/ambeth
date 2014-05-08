using System;
using De.Osthus.Ambeth.Merge.Model;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Merge
{
    public delegate bool ProceedWithMergeHook(ICUDResult result, IList<Object> unpersistedObjectsToDelete);
}

