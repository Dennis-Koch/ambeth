package com.koch.ambeth.merge;

import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IPrimitiveUpdateItem;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.collections.IMap;

public interface ICUDResultHelper
{
	ICUDResult createCUDResult(MergeHandle mergeHandle);

	IPrimitiveUpdateItem[] getEnsureFullPUIs(IEntityMetaData metaData, IMap<Class<?>, IPrimitiveUpdateItem[]> entityTypeToFullPuis);

	IRelationUpdateItem[] getEnsureFullRUIs(IEntityMetaData metaData, IMap<Class<?>, IRelationUpdateItem[]> entityTypeToFullRuis);

	IPrimitiveUpdateItem[] compactPUIs(IPrimitiveUpdateItem[] fullPUIs, int puiCount);

	IRelationUpdateItem[] compactRUIs(IRelationUpdateItem[] fullRUIs, int ruiCount);
}
