package com.koch.ambeth.merge;

import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.service.merge.model.IObjRef;

public interface IMergeListener
{
	ICUDResult preMerge(ICUDResult cudResult, ICache cache);

	void postMerge(ICUDResult cudResult, IObjRef[] updatedObjRefs);
}
