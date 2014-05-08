using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Merge
{
    public interface IMergeController
    {
        void ApplyChangesToOriginals(IList<Object> originalRefs, IList<IObjRef> oriList, DateTime? changedOn, String changedBy);

        ICUDResult MergeDeep(Object obj, MergeHandle handle);

        IList<Object> ScanForInitializedObjects(Object obj, bool isDeepMerge, IMap<Type, IList<Object>> typeToObjectsToMerge, IList<IObjRef> objRefs,
			IList<ValueHolderRef> valueHolderRefs);
    }
}
