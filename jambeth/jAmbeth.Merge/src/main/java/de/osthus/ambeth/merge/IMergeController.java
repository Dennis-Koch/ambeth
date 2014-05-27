package de.osthus.ambeth.merge;

import java.util.List;
import java.util.Map;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.util.ValueHolderRef;

public interface IMergeController
{
	void applyChangesToOriginals(List<Object> originalRefs, List<IObjRef> oriList, Long changedOn, String changedBy);

	ICUDResult mergeDeep(Object obj, MergeHandle handle);

	IList<Object> scanForInitializedObjects(Object obj, boolean isDeepMerge, Map<Class<?>, IList<Object>> typeToObjectsToMerge, List<IObjRef> objRefs,
			List<ValueHolderRef> valueHolderRefs);
}
