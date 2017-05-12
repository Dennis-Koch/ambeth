using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Merge.Incremental;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Merge
{
    public interface ICUDResultApplier
    {
        IIncrementalMergeState AcquireNewState(ICache stateCache);

        ICUDResult ApplyCUDResultOnEntitiesOfCache(ICUDResult cudResult, bool checkBaseState, IIncrementalMergeState incrementalState);
    }
}