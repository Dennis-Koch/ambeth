package de.osthus.ambeth.merge;

import java.util.List;
import java.util.Map;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IOriCollection;
import de.osthus.ambeth.merge.model.IRelationUpdateItem;
import de.osthus.ambeth.merge.model.RelationUpdateItemBuild;
import de.osthus.ambeth.util.ValueHolderRef;

public interface IMergeController
{
	void applyChangesToOriginals(ICUDResult cudResult, IOriCollection oriCollection, ICache cache);

	ICUDResult mergeDeep(Object obj, MergeHandle handle);

	IRelationUpdateItem createRUI(String memberName, List<IObjRef> oldOriList, List<IObjRef> newOriList);

	RelationUpdateItemBuild createRUIBuild(String memberName, List<IObjRef> oldOriList, List<IObjRef> newOriList);

	IList<Object> scanForInitializedObjects(Object obj, boolean isDeepMerge, Map<Class<?>, IList<Object>> typeToObjectsToMerge, List<IObjRef> objRefs,
			List<IObjRef> privilegedObjRefs, List<ValueHolderRef> valueHolderRefs);
}
