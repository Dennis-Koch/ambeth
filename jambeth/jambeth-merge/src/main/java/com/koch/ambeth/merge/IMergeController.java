package com.koch.ambeth.merge;

import java.util.List;
import java.util.Map;

import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IOriCollection;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.merge.model.RelationUpdateItemBuild;
import com.koch.ambeth.merge.util.ValueHolderRef;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.IList;

public interface IMergeController
{
	void applyChangesToOriginals(ICUDResult cudResult, IOriCollection oriCollection, ICache cache);

	ICUDResult mergeDeep(Object obj, MergeHandle handle);

	IRelationUpdateItem createRUI(String memberName, List<IObjRef> oldOriList, List<IObjRef> newOriList);

	RelationUpdateItemBuild createRUIBuild(String memberName, List<IObjRef> oldOriList, List<IObjRef> newOriList);

	IList<Object> scanForInitializedObjects(Object obj, boolean isDeepMerge, Map<Class<?>, IList<Object>> typeToObjectsToMerge, List<IObjRef> objRefs,
			List<IObjRef> privilegedObjRefs, List<ValueHolderRef> valueHolderRefs);
}
