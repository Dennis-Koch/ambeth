package de.osthus.ambeth.merge;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.merge.incremental.IIncrementalMergeState;
import de.osthus.ambeth.merge.model.ICUDResult;

public interface ICUDResultApplier
{
	IIncrementalMergeState acquireNewState(ICache stateCache);

	ICUDResult applyCUDResultOnEntitiesOfCache(ICUDResult cudResult, boolean checkBaseState, final IIncrementalMergeState incrementalState);
}