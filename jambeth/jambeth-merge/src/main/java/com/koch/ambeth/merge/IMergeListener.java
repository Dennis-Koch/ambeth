package com.koch.ambeth.merge;

import com.koch.ambeth.merge.incremental.IIncrementalMergeState;
import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.service.merge.model.IObjRef;

public interface IMergeListener {
	ICUDResult preMerge(ICUDResult cudResult, IIncrementalMergeState incrementalMergeState);

	void postMerge(ICUDResult cudResult, IObjRef[] updatedObjRefs,
			IIncrementalMergeState incrementalMergeState);
}
