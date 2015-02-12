package de.osthus.ambeth.merge;

import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IPrimitiveUpdateItem;
import de.osthus.ambeth.merge.model.IRelationUpdateItem;

public interface ICUDResultHelper
{
	ICUDResult createCUDResult(MergeHandle mergeHandle);

	IPrimitiveUpdateItem[] getEnsureFullPUIs(IEntityMetaData metaData, IMap<Class<?>, IPrimitiveUpdateItem[]> entityTypeToFullPuis);

	IRelationUpdateItem[] getEnsureFullRUIs(IEntityMetaData metaData, IMap<Class<?>, IRelationUpdateItem[]> entityTypeToFullRuis);

	IPrimitiveUpdateItem[] compactPUIs(IPrimitiveUpdateItem[] fullPUIs, int puiCount);

	IRelationUpdateItem[] compactRUIs(IRelationUpdateItem[] fullRUIs, int ruiCount);
}
