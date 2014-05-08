package de.osthus.ambeth.xml;

import java.util.List;
import java.util.Map;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IMergeController;
import de.osthus.ambeth.merge.MergeHandle;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.util.ValueHolderRef;

public class MergeControllerDummy implements IMergeController
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void applyChangesToOriginals(List<Object> originalRefs, List<IObjRef> oriList, Long changedOn, String changedBy)
	{
	}

	@Override
	public ICUDResult mergeDeep(Object obj, MergeHandle handle)
	{
		return null;
	}

	@Override
	public IList<Object> scanForInitializedObjects(Object obj, boolean isDeepMerge, Map<Class<?>, IList<Object>> typeToObjectsToMerge, List<IObjRef> objRefs,
			List<ValueHolderRef> valueHolderRefs)
	{
		return null;
	}
}
