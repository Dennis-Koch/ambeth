using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Merge.Model;
using System;

namespace De.Osthus.Ambeth.Merge
{
    public interface ICUDResultHelper
    {
        ICUDResult CreateCUDResult(MergeHandle mergeHandle);

        IPrimitiveUpdateItem[] GetEnsureFullPUIs(IEntityMetaData metaData, IMap<Type, IPrimitiveUpdateItem[]> entityTypeToFullPuis);

        IRelationUpdateItem[] GetEnsureFullRUIs(IEntityMetaData metaData, IMap<Type, IRelationUpdateItem[]> entityTypeToFullRuis);

	    IPrimitiveUpdateItem[] CompactPUIs(IPrimitiveUpdateItem[] fullPUIs, int puiCount);

	    IRelationUpdateItem[] CompactRUIs(IRelationUpdateItem[] fullRUIs, int ruiCount);
    }
}
