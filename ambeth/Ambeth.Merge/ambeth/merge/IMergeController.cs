using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Merge
{
    public interface IMergeController
    {
        void ApplyChangesToOriginals(ICUDResult cudResult, IOriCollection oriCollection, ICache cache);

        ICUDResult MergeDeep(Object obj, MergeHandle handle);

        IRelationUpdateItem CreateRUI(String memberName, IList<IObjRef> oldOriList, IList<IObjRef> newOriList);

        RelationUpdateItemBuild CreateRUIBuild(String memberName, IList<IObjRef> oldOriList, IList<IObjRef> newOriList);

        IList<Object> ScanForInitializedObjects(Object obj, bool isDeepMerge, IMap<Type, IList<Object>> typeToObjectsToMerge, IList<IObjRef> objRefs,
			IList<ValueHolderRef> valueHolderRefs);
    }
}
