package com.koch.ambeth.merge;

import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.incremental.IIncrementalMergeState;
import com.koch.ambeth.merge.model.ICUDResult;

public interface ICUDResultApplier
{
	IIncrementalMergeState acquireNewState(ICache stateCache);

	ICUDResult applyCUDResultOnEntitiesOfCache(ICUDResult cudResult, boolean checkBaseState, final IIncrementalMergeState incrementalState);
}