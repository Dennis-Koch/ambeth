package de.osthus.ambeth.merge;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IObjRef;

public interface IMergeListener
{
	ICUDResult preMerge(ICUDResult cudResult, ICache cache);

	void postMerge(ICUDResult cudResult, IObjRef[] updatedObjRefs);
}
